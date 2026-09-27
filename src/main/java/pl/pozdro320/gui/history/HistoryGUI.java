package pl.pozdro320.gui.history;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.pozdro320.PozdroSprawdzanieMain;
import pl.pozdro320.gui.holder.HistoryHolder;
import pl.pozdro320.helper.MessageHelper;
import pl.pozdro320.manager.GuiManager;
import pl.pozdro320.models.HistoryEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryGUI {

    private static final String GUI_NAME = "history";

    private final PozdroSprawdzanieMain plugin;
    private final NamespacedKey actionKey;

    private static final int[] HISTORY_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    };

    public HistoryGUI(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
        this.actionKey = new NamespacedKey(plugin, "history_action");
    }

    public void openGUI(Player player, Player target) {
        String targetName = target.getName();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<HistoryEntry> logs = plugin.getHistoryManager().getHistory(targetName);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                GuiManager guiManager = plugin.getConfigManager().getGui();
                int size = guiManager.getGuiSize(GUI_NAME);
                String rawTitle = guiManager.getGuiTitle(GUI_NAME);
                String title = MessageHelper.colored(rawTitle.replace("{PLAYER}", targetName));

                HistoryHolder holder = new HistoryHolder(target.getUniqueId(), targetName);
                Inventory inv = Bukkit.createInventory(holder, size, title);
                holder.setInventory(inv);

                if (logs.isEmpty()) {
                    guiManager.setItem(inv, GUI_NAME, "empty", "EMPTY_LOGS", actionKey, Map.of("{PLAYER}", targetName));
                } else {
                    for (int i = 0; i < logs.size() && i < HISTORY_SLOTS.length; i++) {
                        HistoryEntry entry = logs.get(i);

                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("{PLAYER}", targetName);
                        placeholders.put("{DATE}", entry.date());
                        placeholders.put("{ACTION}", entry.action());
                        placeholders.put("{MODERATOR}", entry.moderator());

                        ItemStack entryItem = guiManager.setItem(GUI_NAME, "entries", "ENTRY_" + i, actionKey, placeholders);
                        inv.setItem(HISTORY_SLOTS[i], entryItem);
                    }
                }

                guiManager.setItem(inv, GUI_NAME, "back", "BACK_TO_CHECKER", actionKey, Map.of("{PLAYER}", targetName));
                player.openInventory(inv);
            });
        });
    }

    public NamespacedKey getActionKey() {
        return actionKey;
    }
}