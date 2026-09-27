package pl.pozdro320.listener.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.pozdro320.PozdroSprawdzanieMain;
import pl.pozdro320.helper.MessageHelper;

public class ChatListener implements Listener {

    private final PozdroSprawdzanieMain plugin;

    public ChatListener(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();

        if (plugin.isChecked(sender)) {
            event.setCancelled(true);
            event.getRecipients().clear();

            Player mod = plugin.getModerator(sender);
            if (mod != null && mod.isOnline()) {
                String rawFormat = plugin.getConfigManager().getMessages().getRawMessage("chat-checker", "{PLAYER}", sender.getName());
                String finalMsg = MessageHelper.colored(rawFormat.replace("{MESSAGE}", message));

                sender.sendMessage(finalMsg);
                mod.sendMessage(finalMsg);
            } else {
                plugin.getConfigManager().getMessages().sendMessages(sender, "errors.chat-no-moderator", "{PLAYER}", sender.getName());
            }
            return;
        }

        Player checked = plugin.getChecked(sender);
        if (checked != null && checked.isOnline()) {
            event.setCancelled(true);
            event.getRecipients().clear();

            String rawFormat = plugin.getConfigManager().getMessages().getRawMessage("chat-moderator", "{MODERATOR}", sender.getName());
            String finalMsg = MessageHelper.colored(rawFormat.replace("{MESSAGE}", message));

            checked.sendMessage(finalMsg);
            sender.sendMessage(finalMsg);
            return;
        }

        event.getRecipients().removeIf(recipient ->
            plugin.isChecked(recipient) || plugin.getChecked(recipient) != null
        );
    }
}