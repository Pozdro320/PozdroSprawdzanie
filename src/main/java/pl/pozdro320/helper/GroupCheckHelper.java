package pl.pozdro320.helper;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import pl.pozdro320.PozdroSprawdzanieMain;

public class GroupCheckHelper {

    private final PozdroSprawdzanieMain plugin;

    public GroupCheckHelper(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
    }

    public String getPrimaryGroup(Player player) {
        Permission perms = plugin.getPerms();
        if (perms == null || player == null) return "default";

        try {
            String group = perms.getPrimaryGroup(player);
            return group != null ? group : "default";
        } catch (Exception e) {
            return "default";
        }
    }

    public boolean canCheck(Player moderator, Player target) {
        if (moderator.isOp()) return true;

        String modGroup = getPrimaryGroup(moderator);
        String targetGroup = getPrimaryGroup(target);

        return plugin.getConfigManager().getHierarchy().canCheck(modGroup, targetGroup);
    }
}