package pl.pozdro320.database.impl;

import org.bukkit.configuration.file.YamlConfiguration;
import pl.pozdro320.PozdroSprawdzanieMain;
import pl.pozdro320.database.HistoryStorage;
import pl.pozdro320.models.HistoryEntry;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlHistoryStorage implements HistoryStorage {

    private final PozdroSprawdzanieMain plugin;
    private final File historyFolder;

    public YamlHistoryStorage(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
        this.historyFolder = new File(plugin.getDataFolder(), "history");
    }

    @Override
    public void init() {
        if (!this.historyFolder.exists()) {
            this.historyFolder.mkdirs();
        }
    }

    @Override
    public void log(String playerName, String action, String moderatorName, String time) {
        File playerFile = new File(historyFolder, playerName + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("date", time);
        entry.put("action", action);
        entry.put("moderator", moderatorName);

        List<Map<?, ?>> logs = new ArrayList<>(config.getMapList("entries"));
        logs.add(entry);

        config.set("entries", logs);
        config.set("last_update", time);

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie udalo sie zapisac historii w pliku dla: " + playerName);
            e.printStackTrace();
        }
    }

    @Override
    public List<HistoryEntry> getHistory(String playerName) {
        File playerFile = new File(historyFolder, playerName + ".yml");
        if (!playerFile.exists()) return Collections.emptyList();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        List<Map<?, ?>> rawEntries = config.getMapList("entries");
        if (rawEntries.isEmpty()) return Collections.emptyList();

        List<HistoryEntry> entries = new ArrayList<>();
        for (int i = rawEntries.size() - 1; i >= 0; i--) {
            Map<?, ?> map = rawEntries.get(i);

            Object dateObj = map.get("date");
            Object actionObj = map.get("action");
            Object modObj = map.get("moderator");

            entries.add(new HistoryEntry(
                    dateObj != null ? dateObj.toString() : "Brak daty",
                    actionObj != null ? actionObj.toString() : "Brak akcji",
                    modObj != null ? modObj.toString() : "Brak moderatora"
            ));
        }

        return entries;
    }

    @Override
    public void close() {
    }
}