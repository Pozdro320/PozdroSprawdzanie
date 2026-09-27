package pl.pozdro320.listener.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import pl.pozdro320.PozdroSprawdzanieMain;
import pl.pozdro320.gui.holder.HistoryHolder;

public class HistoryGuiListener implements Listener {

    private final PozdroSprawdzanieMain plugin;

    public HistoryGuiListener(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof HistoryHolder holder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player moderator)) return;

        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getInventory().getSize()) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        String action = meta.getPersistentDataContainer().get(plugin.getHistoryGUI().getActionKey(), PersistentDataType.STRING);
        if (action == null) return;

        Player target = Bukkit.getPlayer(holder.getTargetUuid());
        if (target == null || !target.isOnline()) {
            moderator.closeInventory();
            plugin.getConfigManager().getMessages().sendMessages(moderator, "errors.player-offline", "{PLAYER}", holder.getTargetName());
            return;
        }

        switch (action) {
            case "PREVIOUS_PAGE" -> {
                plugin.getHistoryGUI().openGUI(moderator, target, holder.getPage() - 1);
                moderator.playSound(moderator.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            }
            case "NEXT_PAGE" -> {
                plugin.getHistoryGUI().openGUI(moderator, target, holder.getPage() + 1);
                moderator.playSound(moderator.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            }
            case "BACK_TO_CHECKER" -> {
                plugin.getCheckerGUI().openGUI(moderator, target);
                moderator.playSound(moderator.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            }
        }
    }
}