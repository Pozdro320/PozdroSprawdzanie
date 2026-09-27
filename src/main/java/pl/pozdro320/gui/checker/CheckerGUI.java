package pl.pozdro320.gui.checker;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.pozdro320.PozdroSprawdzanieMain;
import pl.pozdro320.gui.holder.CheckerHolder;
import pl.pozdro320.helper.MessageHelper;
import pl.pozdro320.manager.GuiManager;

import java.util.Map;

public class CheckerGUI {

    private static final String GUI_NAME = "checker";

    private final PozdroSprawdzanieMain plugin;
    private final NamespacedKey actionKey;

    public CheckerGUI(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
        this.actionKey = new NamespacedKey(plugin, "checker_action");
    }

    public void openGUI(Player player, Player target) {
        GuiManager guiManager = plugin.getConfigManager().getGui();
        String targetName = target.getName();

        int size = guiManager.getGuiSize(GUI_NAME);
        String rawTitle = guiManager.getGuiTitle(GUI_NAME);
        String title = MessageHelper.colored(rawTitle.replace("{PLAYER}", targetName));

        CheckerHolder holder = new CheckerHolder(target.getUniqueId(), targetName);
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inv);

        Map<String, String> placeholders = Map.of("{PLAYER}", targetName);

        guiManager.setItem(inv, GUI_NAME, "check", "ACTION_CHECK", actionKey, placeholders);
        guiManager.setItem(inv, GUI_NAME, "clean", "ACTION_CLEAN", actionKey, placeholders);
        guiManager.setItem(inv, GUI_NAME, "cheater", "ACTION_CHEATER", actionKey, placeholders);
        guiManager.setItem(inv, GUI_NAME, "admission", "ACTION_ADMISSION", actionKey, placeholders);
        guiManager.setItem(inv, GUI_NAME, "lack-of-cooperation", "ACTION_LACK_COOPERATION", actionKey, placeholders);
        guiManager.setItem(inv, GUI_NAME, "teleport", "ACTION_TELEPORT", actionKey, placeholders);
        guiManager.setItem(inv, GUI_NAME, "history", "ACTION_HISTORY", actionKey, placeholders);

        player.openInventory(inv);
    }

    public NamespacedKey getActionKey() {
        return actionKey;
    }
}