package pl.pozdro320.database;

import pl.pozdro320.models.HistoryEntry;

import java.util.List;

public interface HistoryStorage {
    void init();
    void log(String playerName, String action, String moderatorName, String time);
    List<HistoryEntry> getHistory(String playerName);
    void close();
}