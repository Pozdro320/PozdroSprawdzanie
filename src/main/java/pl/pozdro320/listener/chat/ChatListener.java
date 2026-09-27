package pl.pozdro320.listener.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import pl.pozdro320.PozdroSprawdzanieMain;

public class ChatListener implements Listener {

    private final PozdroSprawdzanieMain plugin;

    public ChatListener(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player moderator = event.getPlayer();
        String message = event.getMessage();

        if (plugin.isChecked(moderator)) {
            Player checker = moderator;
            event.setCancelled(true);
            Player mod = plugin.getModerator(checker);

            if (mod != null && mod.isOnline()) {
                String rawFormat = plugin.getConfigManager().getMessages().getRawMessage("chat-checker", "{PLAYER}", checker.getName());
                String finalMsg = rawFormat.replace("{MESSAGE}", message);

                checker.sendMessage(finalMsg);
                mod.sendMessage(finalMsg);
            } else {
                plugin.getConfigManager().getMessages().sendMessages(checker, "chat-chat-no-moderator", "{PLAYER}", checker.getName());
            }
            return;
        }

        Player checked = plugin.getChecked(moderator);
        if (checked != null && checked.isOnline()) {
            event.setCancelled(true);

            String rawFormat = plugin.getConfigManager().getMessages().getRawMessage("chat-moderator", "{MODERATOR}", moderator.getName());
            String finalMsg = rawFormat.replace("{MESSAGE}", message);

            checked.sendMessage(finalMsg);
            moderator.sendMessage(finalMsg);
        }
    }
}