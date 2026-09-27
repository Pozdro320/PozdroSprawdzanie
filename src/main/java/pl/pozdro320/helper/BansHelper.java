package pl.pozdro320.helper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.pozdro320.PozdroSprawdzanieMain;

import java.util.List;

public class BansHelper {

    private final PozdroSprawdzanieMain plugin;

    public BansHelper(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
    }

    public void executeBan(Player target, Player moderator, String type) {
        String targetName = target.getName();
        String modName = (moderator != null) ? moderator.getName() : "Console";

        List<String> banCommands = plugin.getConfigManager().getBanCommands(type);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (banCommands == null || banCommands.isEmpty()) return;

            for (String cmd : banCommands) {
                String finalCmd = cmd.replace("{PLAYER}", targetName).replace("{MODERATOR}", modName);
                if (finalCmd.startsWith("/")) {
                    finalCmd = finalCmd.substring(1);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
            }
        }, 20L);
    }
}