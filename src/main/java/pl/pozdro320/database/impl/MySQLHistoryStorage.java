package pl.pozdro320.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import pl.pozdro320.PozdroSprawdzanieMain;
import pl.pozdro320.database.HistoryStorage;
import pl.pozdro320.models.HistoryEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MySQLHistoryStorage implements HistoryStorage {

    private final PozdroSprawdzanieMain plugin;
    private final String tableName;
    private HikariDataSource dataSource;

    public MySQLHistoryStorage(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
        String prefix = plugin.getConfig().getString("database.table-prefix", "pozdro_");
        this.tableName = prefix + "history";
    }

    @Override
    public void init() {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.host", "127.0.0.1");
        int port = config.getInt("database.port", 4837);
        String database = config.getString("database.database", "minecraft");
        String user = config.getString("database.username", "root");
        String pass = config.getString("database.password", "");
        boolean ssl = config.getBoolean("database.ssl", false);

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + ssl + "&allowPublicKeyRetrieval=true&characterEncoding=utf8&rewriteBatchedStatements=true");
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(pass);
        hikariConfig.setPoolName("PozdroSprawdzanie-Pool");

        hikariConfig.setMaximumPoolSize(config.getInt("database.pool.maximum-pool-size", 12));
        hikariConfig.setMinimumIdle(config.getInt("database.pool.minimum-idle", 3));
        hikariConfig.setConnectionTimeout(config.getLong("database.pool.connection-timeout", 5000));
        hikariConfig.setIdleTimeout(300000);
        hikariConfig.setMaxLifetime(config.getLong("database.pool.max-lifetime", 600000));

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

        this.dataSource = new HikariDataSource(hikariConfig);

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                    "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "  `player_name` VARCHAR(16) NOT NULL," +
                    "  `action` VARCHAR(128) NOT NULL," +
                    "  `moderator` VARCHAR(16) NOT NULL," +
                    "  `date` VARCHAR(32) NOT NULL," +
                    "  INDEX `idx_player_id` (`player_name`, `id` DESC)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")) {
                ps.executeUpdate();
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE INDEX `idx_player_id` ON `" + tableName + "` (`player_name`, `id` DESC);");
            } catch (SQLException ignored) {
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Nie udalo sie zainicjalizowac tabeli bazy danych!");
            e.printStackTrace();
        }
    }

    @Override
    public void log(String playerName, String action, String moderatorName, String time) {
        Runnable logTask = () -> {
            String query = "INSERT INTO `" + tableName + "` (`player_name`, `action`, `moderator`, `date`) VALUES (?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, playerName);
                ps.setString(2, action);
                ps.setString(3, moderatorName);
                ps.setString(4, time);
                ps.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().severe("Blad zapisu historii do bazy dla gracza: " + playerName);
                e.printStackTrace();
            }
        };

        if (plugin.isEnabled() && Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, logTask);
        } else {
            logTask.run();
        }
    }

    @Override
    public List<HistoryEntry> getHistory(String playerName) {
        String query = "SELECT `date`, `action`, `moderator` FROM `" + tableName + "` WHERE `player_name` = ? ORDER BY `id` DESC";
        List<HistoryEntry> entries = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, playerName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(new HistoryEntry(
                            rs.getString("date"),
                            rs.getString("action"),
                            rs.getString("moderator")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Blad pobierania historii z bazy dla gracza: " + playerName);
            e.printStackTrace();
            return Collections.emptyList();
        }

        return entries;
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}