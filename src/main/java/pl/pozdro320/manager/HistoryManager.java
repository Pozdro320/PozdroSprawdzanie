package pl.pozdro320.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.pozdro320.PozdroSprawdzanieMain;
import pl.pozdro320.models.HistoryEntry;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HistoryManager {

    private final PozdroSprawdzanieMain plugin;
    private final File historyFolder;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public HistoryManager(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
        this.historyFolder = new File(plugin.getDataFolder(), "history");
        if (!this.historyFolder.exists()) {
            this.historyFolder.mkdirs();
        }
    }

    public void log(String playerName, String action, String moderatorName) {
        String time = LocalDateTime.now().format(formatter);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
                plugin.getLogger().severe("Nie udalo sie zapisac historii dla: " + playerName);
                e.printStackTrace();
            }
        });
    }

    public List<HistoryEntry> getHistory(String playerName) {
        File playerFile = new File(historyFolder, playerName + ".yml");
        if (!playerFile.exists()) return Collections.emptyList();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        List<Map<?, ?>> rawEntries = config.getMapList("entries");

        if (rawEntries.isEmpty()) return Collections.emptyList();

        List<HistoryEntry> entries = new ArrayList<>();
        int limit = 14;
        int start = Math.max(0, rawEntries.size() - limit);

        for (int i = rawEntries.size() - 1; i >= start; i--) {
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
}