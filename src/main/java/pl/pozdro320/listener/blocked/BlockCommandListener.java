package pl.pozdro320.listener.blocked;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import pl.pozdro320.PozdroSprawdzanieMain;

public class BlockCommandListener implements Listener {

    private final PozdroSprawdzanieMain plugin;

    public BlockCommandListener(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isChecked(player)) return;

        List<String> allowedCommands = plugin.getConfigManager().getAllowedCommands();
        if (allowedCommands == null) return;

        String rawMessage = event.getMessage().toLowerCase();
        String mainCommand = rawMessage.split(" ")[0];

        if (allowedCommands.contains(mainCommand)) {
            return;
        }

        event.setCancelled(true);
        plugin.getConfigManager().getMessages().sendMessages(player, "errors.block-commands");
    }

}
