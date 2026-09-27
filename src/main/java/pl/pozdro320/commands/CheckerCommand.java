package pl.pozdro320.commands;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import pl.pozdro320.PozdroSprawdzanieMain;
import pl.pozdro320.utils.TabCompleteUtil;

public class CheckerCommand extends Command {

    private final PozdroSprawdzanieMain plugin;

    public CheckerCommand(PozdroSprawdzanieMain plugin, String name) {
        super(name);
        this.plugin = plugin;

        this.setDescription("Komenda zarządzająca sprawdzarką");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            plugin.getConfigManager().getMessages().sendMessages(sender, "errors.only-players");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            plugin.getConfigManager().getMessages().sendMessages(player, "usage.checker", "{PLAYER}", player.getName());
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "set-checker" -> {
                if (!player.hasPermission("pozdrosprawdzanie.admin")) {
                    plugin.getConfigManager().getMessages().sendMessages(player, "errors.no-permission-checker", "{PLAYER}", player.getName());
                    return true;
                }

                Location loc = player.getLocation();
                plugin.setCheckLocation(loc);

                plugin.getConfigManager().getMessages().sendMessages(player, "locations-settings.set-checker");

                DustOptions redDust = new DustOptions(Color.RED, 1f);
                player.getWorld().spawnParticle(Particle.DUST, loc.add(0, 1, 0), 30, 0.5, 1, 0.5, redDust);
            }

            case "set-spawn" -> {
                if (!player.hasPermission("pozdrosprawdzanie.admin")) {
                    plugin.getConfigManager().getMessages().sendMessages(player, "errors.no-permission-checker", "{PLAYER}", player.getName());
                    return true;
                }

                Location loc = player.getLocation();
                plugin.setSpawnLocation(loc);

                plugin.getConfigManager().getMessages().sendMessages(player, "locations-settings.set-spawn");

                DustOptions yellowDust = new DustOptions(Color.YELLOW, 1f);
                player.getWorld().spawnParticle(Particle.DUST, loc.add(0, 1, 0), 30, 0.5, 1, 0.5, yellowDust);
            }

            case "reload" -> {
                if (!player.hasPermission("pozdrosprawdzanie.admin")) {
                    plugin.getConfigManager().getMessages().sendMessages(player, "errors.no-permission-checker", "{PLAYER}", player.getName());
                    return true;
                }

                plugin.reloadPlugin();
                plugin.getConfigManager().getMessages().sendMessages(player, "reload-success");
            }

            case "teleport" -> {
                if (!player.hasPermission("pozdrosprawdzanie.sprawdz")) {
                    plugin.getConfigManager().getMessages().sendMessages(player, "errors.no-permission-check");
                    return true;
                }

                Location checkLoc = plugin.getCheckLocation();
                if (checkLoc == null) {
                    plugin.getConfigManager().getMessages().sendMessages(player, "errors.no-checker");
                    return true;
                }

                player.teleport(checkLoc);
                plugin.getConfigManager().getMessages().sendMessages(player, "tp-checker");
            }

            default -> {
                plugin.getConfigManager().getMessages().sendMessages(player, "usage.checker");
            }
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(
            @NotNull CommandSender sender,
            @NotNull String alias,
            @NotNull String[] args
    ) {

        Map<String, String> options = Map.of(
                "set-checker", "pozdrosprawdzanie.admin",
                "set-spawn", "pozdrosprawdzanie.admin",
                "reload", "pozdrosprawdzanie.admin",
                "teleport", "pozdrosprawdzanie.sprawdz"
        );

        return TabCompleteUtil.tab(sender, args, options);
    }

}