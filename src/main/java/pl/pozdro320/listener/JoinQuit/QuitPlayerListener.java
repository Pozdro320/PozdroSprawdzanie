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
            plugin.getHistoryManager().log(player.getName(), logoutReason, modName);

            if (moderator != null && moderator.isOnline()) {
                plugin.getConfigManager().getMessages().sendMessages(
                    moderator,
                    "logged-out-mod",
                    "{PLAYER}", player.getName(),
                    "{MODERATOR}", modName
                );
            }

            plugin.getBansHelper().executeBan(player, moderator, "logged-out");
            plugin.removeChecked(player);
            return;
        }

        Player checked = plugin.getChecked(player);
        if (checked != null) {
            plugin.removeChecked(checked);

            if (checked.isOnline()) {
                if (plugin.getSpawnLocation() != null) {
                    checked.teleport(plugin.getSpawnLocation());
                }
                plugin.getConfigManager().getMessages().sendMessages(
                    checked,
                    "mod-left-during-check",
                    "{MODERATOR}", player.getName()
                );
            }
        }
    }
}