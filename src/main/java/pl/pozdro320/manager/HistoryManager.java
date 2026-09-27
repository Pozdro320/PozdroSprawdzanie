package pl.pozdro320.manager;

import org.bukkit.Bukkit;
import pl.pozdro320.PozdroSprawdzanieMain;
import pl.pozdro320.database.HistoryStorage;
import pl.pozdro320.database.impl.MySQLHistoryStorage;
import pl.pozdro320.database.impl.YamlHistoryStorage;
import pl.pozdro320.models.HistoryEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HistoryManager {

    private final PozdroSprawdzanieMain plugin;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");
    private HistoryStorage storage;

    public HistoryManager(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
        this.initStorage();
    }

    public void initStorage() {
        if (this.storage != null) {
            this.storage.close();
        }

        String type = plugin.getConfig().getString("database.type", "YML").toUpperCase();
        if (type.equals("MYSQL") || type.equals("MARIADB")) {
            this.storage = new MySQLHistoryStorage(plugin);
            plugin.getLogger().info("Zaladowano obsluge bazy danych MySQL/MariaDB dla historii.");
        } else {
            this.storage = new YamlHistoryStorage(plugin);
            plugin.getLogger().info("Zaladowano obsluge plikow YML dla historii.");
        }

        this.storage.init();
    }

    /**
     * Asynchroniczny zapis logu do bazy lub pliku.
     */
    public void log(String playerName, String action, String moderatorName) {
        String time = LocalDateTime.now().format(formatter);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            storage.log(playerName, action, moderatorName, time);
        });
    }

    /**
     * Synchroniczne pobranie danych
     */
    public List<HistoryEntry> getHistory(String playerName) {
        return storage.getHistory(playerName);
    }

    /**
     * Asynchroniczne pobranie całej historii.
     */
    public CompletableFuture<List<HistoryEntry>> getHistoryAsync(String playerName) {
        return CompletableFuture.supplyAsync(() -> storage.getHistory(playerName));
    }

    public void shutdown() {
        if (storage != null) {
            storage.close();
        }
    }
}