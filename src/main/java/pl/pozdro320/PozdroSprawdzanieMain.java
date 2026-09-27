package pl.pozdro320;

import lombok.Getter;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import pl.pozdro320.api.PozdroSprawdzanieAPI;
import pl.pozdro320.commands.CheckCommand;
import pl.pozdro320.commands.CheckerCommand;
import pl.pozdro320.commands.ConfessesCommand;
import pl.pozdro320.gui.checker.CheckerGUI;
import pl.pozdro320.gui.history.HistoryGUI;
import pl.pozdro320.helper.BansHelper;
import pl.pozdro320.helper.GroupCheckHelper;
import pl.pozdro320.helper.VisibilityHelper;
import pl.pozdro320.listener.JoinQuit.OnJoinListener;
import pl.pozdro320.listener.JoinQuit.QuitPlayerListener;
import pl.pozdro320.listener.blocked.BlockCommandListener;
import pl.pozdro320.listener.chat.ChatListener;
import pl.pozdro320.listener.gui.CheckedGuiListener;
import pl.pozdro320.listener.gui.HistoryGuiListener;
import pl.pozdro320.manager.ConfigManager;
import pl.pozdro320.manager.HistoryManager;
import pl.pozdro320.task.CheckedPlayersTask;
import pl.pozdro320.utils.RegistryUtil;
import pl.pozdro320.utils.Updater.GithubUpdater;
import pl.pozdro320.utils.Updater.SemanticVersion;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class PozdroSprawdzanieMain extends JavaPlugin {

    private final Map<UUID, UUID> checkedPlayers = new ConcurrentHashMap<>();

    private ConfigManager configManager;
    private HistoryManager historyManager;

    private BansHelper bansHelper;
    private GroupCheckHelper groupCheckHelper;
    private VisibilityHelper visibilityHelper;

    private Location checkLocation;
    private Location spawnLocation;

    private CheckerGUI checkerGUI;
    private HistoryGUI historyGUI;

    private CheckedPlayersTask checkedPlayersTask;
    private Permission perms;

    private boolean updateAvailable = false;
    private static boolean placeholderAPIEnabled = false;

    @Override
    public void onEnable() {
        PozdroSprawdzanieAPI.init(this);

        if (!setupPermissions()) {
            getLogger().severe("Nie znaleziono Vault! Wylaczanie pluginu...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPIEnabled = true;
            getLogger().info("Znaleziono PlaceholderAPI! Integracja zostala wlaczona.");
        } else {
            getLogger().warning("Nie znaleziono PlaceholderAPI. Niektore zmienne moga nie dzialac.");
        }

        this.configManager = new ConfigManager(this);
        this.historyManager = new HistoryManager(this);
        this.bansHelper = new BansHelper(this);
        this.groupCheckHelper = new GroupCheckHelper(this);
        this.visibilityHelper = new VisibilityHelper(this);

        this.checkLocation = configManager.getLocations().load("checker");
        this.spawnLocation = configManager.getLocations().load("spawn");

        this.historyGUI = new HistoryGUI(this);
        this.checkerGUI = new CheckerGUI(this);

        this.checkedPlayersTask = new CheckedPlayersTask(this);
        this.checkedPlayersTask.start();

        new RegistryUtil(this)
            .registerListeners(
                new CheckedGuiListener(this),
                new ChatListener(this),
                new QuitPlayerListener(this),
                new BlockCommandListener(this),
                new HistoryGuiListener(this),
                new OnJoinListener(this)
            )
            .registerCommands(
                new ConfessesCommand(this, "przyznajesie"),
                new CheckCommand(this, "sprawdz"),
                new CheckerCommand(this, "sprawdzarka")
            );

        String updateUrl = "https://gist.githubusercontent.com/Pozdro320/2c46fe72d5a9f822c55f087e00cc47a5/raw/gistfile1.txt";
        checkUpdate(new GithubUpdater(updateUrl));
    }

    @Override
    public void onDisable() {
        for (var entry : checkedPlayers.entrySet()) {
            Player target = Bukkit.getPlayer(entry.getKey());
            Player moderator = Bukkit.getPlayer(entry.getValue());

            if (target != null && spawnLocation != null) {
                target.teleport(spawnLocation);
                target.sendMessage("§cPlugin zostal wylaczony. Sprawdzanie przerwane.");
                visibilityHelper.restoreVisibility(target);
            }
            if (moderator != null) {
                visibilityHelper.restoreVisibility(moderator);
            }
        }

        if (this.checkedPlayersTask != null) {
            this.checkedPlayersTask.stop();
        }

        if (this.historyManager != null) {
            this.historyManager.shutdown();
        }

        checkedPlayers.clear();
    }

    public void reloadPlugin() {
        configManager.reload();

        this.checkLocation = configManager.getLocations().load("checker");
        this.spawnLocation = configManager.getLocations().load("spawn");

        this.historyGUI = new HistoryGUI(this);
        this.checkerGUI = new CheckerGUI(this);
    }

    public void setCheckLocation(Location loc) {
        this.checkLocation = loc;
        configManager.getLocations().save("checker", loc);
    }

    public void setSpawnLocation(Location loc) {
        this.spawnLocation = loc;
        configManager.getLocations().save("spawn", loc);
    }

    public void addChecked(Player target, Player moderator) {
        checkedPlayers.put(target.getUniqueId(), moderator.getUniqueId());
        visibilityHelper.isolatePlayers(moderator, target);
    }

    public void removeChecked(Player target) {
        UUID modUuid = checkedPlayers.remove(target.getUniqueId());

        visibilityHelper.restoreVisibility(target);

        if (modUuid != null) {
            Player moderator = Bukkit.getPlayer(modUuid);
            if (moderator != null && moderator.isOnline()) {
                visibilityHelper.restoreVisibility(moderator);
            }
        }
    }

    public boolean isChecked(Player p) {
        return checkedPlayers.containsKey(p.getUniqueId());
    }

    public Player getModerator(Player checker) {
        UUID modUUID = checkedPlayers.get(checker.getUniqueId());
        return modUUID != null ? Bukkit.getPlayer(modUUID) : null;
    }

    public Player getChecked(Player moderator) {
        UUID modUuid = moderator.getUniqueId();
        for (Map.Entry<UUID, UUID> entry : checkedPlayers.entrySet()) {
            if (entry.getValue().equals(modUuid)) {
                return Bukkit.getPlayer(entry.getKey());
            }
        }
        return null;
    }

    private boolean setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return false;
        this.perms = rsp.getProvider();
        return this.perms != null;
    }

    private void checkUpdate(GithubUpdater updater) {
        SemanticVersion currentVersion = new SemanticVersion(this.getDescription().getVersion());
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            if (updater.hasUpdate(currentVersion)) {
                this.updateAvailable = true;
                getLogger().warning("DOSTEPNA JEST NOWA WERSJA PLUGINU!");
            } else {
                getLogger().info("Uzywasz najnowszej wersji pluginu.");
            }
        });
    }

    public static boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public VisibilityHelper getVisibilityHelper() {
        return visibilityHelper;
    }
}