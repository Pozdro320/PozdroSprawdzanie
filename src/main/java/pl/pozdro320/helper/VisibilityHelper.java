package pl.pozdro320.helper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.pozdro320.PozdroSprawdzanieMain;


public class VisibilityHelper {

    private final PozdroSprawdzanieMain plugin;

    public VisibilityHelper(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
    }

    public void isolatePlayers(Player moderator, Player target) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(moderator) || other.equals(target)) continue;

            moderator.hidePlayer(plugin, other);
            target.hidePlayer(plugin, other);

            other.hidePlayer(plugin, moderator);
            other.hidePlayer(plugin, target);
        }
    }

    public void restoreVisibility(Player player) {
        if (player == null || !player.isOnline()) return;

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;

            player.showPlayer(plugin, other);
            other.showPlayer(plugin, player);
        }
    }


    public void handleJoin(Player joiningPlayer) {
        for (var entry : plugin.getCheckedPlayers().entrySet()) {
            Player target = Bukkit.getPlayer(entry.getKey());
            Player moderator = Bukkit.getPlayer(entry.getValue());

            if (target != null && target.isOnline()) {
                target.hidePlayer(plugin, joiningPlayer);
                joiningPlayer.hidePlayer(plugin, target);
            }

            if (moderator != null && moderator.isOnline()) {
                moderator.hidePlayer(plugin, joiningPlayer);
                joiningPlayer.hidePlayer(plugin, moderator);
            }
        }
    }
}