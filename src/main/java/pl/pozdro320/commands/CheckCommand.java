package pl.pozdro320.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import pl.pozdro320.PozdroSprawdzanieMain;

public class CheckCommand extends Command {

    private final PozdroSprawdzanieMain plugin;

    public CheckCommand(PozdroSprawdzanieMain plugin, String name) {
        super(name);

        this.plugin = plugin;

        this.setDescription("Komenda pozwalająca sprawdzić gracza");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            plugin.getConfigManager().getMessages().sendMessages(sender, "errors.only-players");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("pozdrosprawdzanie.sprawdz")) {
            plugin.getConfigManager().getMessages().sendMessages(player, "errors.no-permission-check");
            return true;
        }

        if (args.length != 1) {
            plugin.getConfigManager().getMessages().sendMessages(player, "usage.check", "{PLAYER}", player.getName());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getConfigManager().getMessages().sendMessages(player, "errors.player-offline", "{PLAYER}", player.getName());

            return true;
        }

        if (target.equals(player)) {
            plugin.getConfigManager().getMessages().sendMessages(player, "errors.him-self");
            return true;
        }

        if (!plugin.getGroupCheckHelper().canCheck(player, target)) {
            plugin.getConfigManager().getMessages().sendMessages(player, "errors.hierarchy", "{PLAYER}", player.getName());
            return true;
        }

        plugin.getCheckerGUI().openGUI(player, target);
        return true;
    }
}
