package pl.pozdro320.listener.JoinQuit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import pl.pozdro320.PozdroSprawdzanieMain;

public class QuitPlayerListener implements Listener {

    private final PozdroSprawdzanieMain plugin;

    public QuitPlayerListener(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.isChecked(player)) {
            Player moderator = plugin.getModerator(player);
            String modName = (moderator != null) ? moderator.getName() : "Console";

            String logoutReason = plugin.getConfig().getString("history-reasons.logout", "BANNED (LOGOUT)");
            plugin.getHistoryManager().log(player.getName(), logoutReason, moderator.getName());

            if (moderator != null && moderator.isOnline()) {
                plugin.getConfigManager().getMessages().sendMessages(moderator, "logged-out-mod", player.getName(), modName);
            }

            plugin.getBansHelper().executeBan(player, moderator, "logged-out");

            plugin.removeChecked(player);
        }
    }
}
