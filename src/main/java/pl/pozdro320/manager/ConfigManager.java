package pl.pozdro320.manager;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.pozdro320.PozdroSprawdzanieMain;
import pl.pozdro320.datacache.HierarchyData;
import pl.pozdro320.helper.MessageHelper;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ConfigManager {

    private final PozdroSprawdzanieMain plugin;
    private final LocationManager locations;

    private MessageManager messages;
    private GuiManager gui;
    private List<String> allowedCommands;
    private HierarchyData hierarchy;
    private FileConfiguration config;
    private FileConfiguration messagesConfig;

    private YamlConfiguration checkerGuiConfig;
    private YamlConfiguration historyGuiConfig;

    public ConfigManager(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
        this.locations = new LocationManager(plugin);

        plugin.saveDefaultConfig();
        saveResourceIfNotExists("messages.yml");
        saveResourceIfNotExists("gui/CheckerGui.yml");
        saveResourceIfNotExists("gui/HistoryGui.yml");

        reload();
    }

    public void reload() {
        MessageHelper.clearCache();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        this.messages = new MessageManager(this.messagesConfig);

        File checkerFile = new File(plugin.getDataFolder(), "gui/CheckerGui.yml");
        this.checkerGuiConfig = YamlConfiguration.loadConfiguration(checkerFile);

        File historyFile = new File(plugin.getDataFolder(), "gui/HistoryGui.yml");
        this.historyGuiConfig = YamlConfiguration.loadConfiguration(historyFile);

        if (this.gui == null) {
            this.gui = new GuiManager();
        } else {
            this.gui.clear();
        }

        this.gui.loadConfig(this.checkerGuiConfig);
        this.gui.loadConfig(this.historyGuiConfig);

        this.allowedCommands = config.getStringList("allowed-commands").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        this.hierarchy = initHierarchyData(this.config);
    }

    private HierarchyData initHierarchyData(FileConfiguration config) {
        boolean enabled = config.getBoolean("hierarchy.enabled", true);
        Map<String, Integer> priorities = new HashMap<>();

        ConfigurationSection section = config.getConfigurationSection("hierarchy.rang");
        if (section != null) {
            for (String group : section.getKeys(false)) {
                priorities.put(group.toLowerCase(), section.getInt(group));
            }
        }
        return new HierarchyData(priorities, enabled);
    }

    public List<String> getBanCommands(String type) {
        return config.getStringList("commands." + type);
    }

    private void saveResourceIfNotExists(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }
}