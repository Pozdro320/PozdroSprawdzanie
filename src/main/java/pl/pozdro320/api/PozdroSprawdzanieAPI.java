package pl.pozdro320.api;

import org.bukkit.entity.Player;
import pl.pozdro320.PozdroSprawdzanieMain;

import java.util.UUID;

public final class PozdroSprawdzanieAPI {

    private static PozdroSprawdzanieMain plugin;

    private PozdroSprawdzanieAPI() {}

    public static void init(PozdroSprawdzanieMain instance) {
        plugin = instance;
    }

    /**
     * Sprawdza, czy gracz jest wzywany/sprawdzany.
     */
    public static boolean isChecked(UUID uuid) {
        return plugin != null && plugin.getCheckedPlayers().containsKey(uuid);
    }

    public static boolean isChecked(Player player) {
        return player != null && isChecked(player.getUniqueId());
    }

    /**
     * Sprawdza, czy dany gracz jest moderatorem prowadzącym sprawdzanie.
     */
    public static boolean isModerator(UUID uuid) {
        return plugin != null && plugin.getCheckedPlayers().containsValue(uuid);
    }

    public static boolean isModerator(Player player) {
        return player != null && isModerator(player.getUniqueId());
    }

    /**
     * Sprawdza, czy gracz bierze udział w sprawdzaniu (jako sprawdzany LUB jako moderator).
     * Idealne do blokowania AFK, teleportacji i czatu globalnego.
     */
    public static boolean isInCheckSession(UUID uuid) {
        return isChecked(uuid) || isModerator(uuid);
    }

    public static boolean isInCheckSession(Player player) {
        return player != null && isInCheckSession(player.getUniqueId());
    }

    /**
     * Zwraca gracza sprawdzanego przez danego moderatora.
     */
    public static Player getChecked(Player moderator) {
        return plugin != null ? plugin.getChecked(moderator) : null;
    }

    /**
     * Zwraca moderatora przypisanego do sprawdzanego gracza.
     */
    public static Player getModerator(Player checked) {
        return plugin != null ? plugin.getModerator(checked) : null;
    }
}