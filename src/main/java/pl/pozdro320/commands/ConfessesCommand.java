package pl.pozdro320.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import pl.pozdro320.PozdroSprawdzanieMain;

public class ConfessesCommand extends Command {

    private final PozdroSprawdzanieMain plugin;

    public ConfessesCommand(PozdroSprawdzanieMain plugin, String name) {
        super(name);
        this.plugin = plugin;

        this.setDescription("Komenda pozwalająca przyznać się podczas sprawdzania");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            plugin.getConfigManager().getMessages().sendMessages(sender, "errors.only-players");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.isChecked(player)) {
            plugin.getConfigManager().getMessages().sendMessages(player, "errors.not-checked");
            return true;
        }

        Player moderator = plugin.getModerator(player);

        Location spawnLoc = plugin.getSpawnLocation();

        player.teleport(spawnLoc);
        plugin.removeChecked(player);

        if (moderator != null && moderator.isOnline()) {
            plugin.getConfigManager().getMessages().sendMessages(moderator, "admission-mod", "{PLAYER}", player.getName());
        }

        plugin.removeChecked(player);
        player.teleport(plugin.getSpawnLocation());

        plugin.getBansHelper().executeBan(player, moderator, "admission");

        return true;
    }
}
