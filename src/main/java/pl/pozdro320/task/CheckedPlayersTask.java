package pl.pozdro320.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import pl.pozdro320.PozdroSprawdzanieMain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CheckedPlayersTask {

    private final PozdroSprawdzanieMain plugin;
    private BukkitTask task;

    public CheckedPlayersTask(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (this.task != null) return;

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                executeTaskLogic();
            }
        }.runTaskTimer(plugin, 0L, 60L);
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    private void executeTaskLogic() {
        Map<UUID, UUID> activePlayers = new HashMap<>(plugin.getCheckedPlayers());

        if (activePlayers.isEmpty()) return;

        for (Map.Entry<UUID, UUID> entry : activePlayers.entrySet()) {
            Player target = Bukkit.getPlayer(entry.getKey());
            Player moderator = Bukkit.getPlayer(entry.getValue());

            if (target == null || !target.isOnline()) continue;

            String targetName = target.getName();
            String modName = (moderator != null) ? moderator.getName() : "Console";

            plugin.getConfigManager().getMessages().sendMessages(target, "player-message", "{MODERATOR}", modName);

            if (moderator != null && moderator.isOnline()) {
                plugin.getConfigManager().getMessages().sendMessages(moderator, "mod-message", "{PLAYER}", targetName);
            }
        }
    }
}