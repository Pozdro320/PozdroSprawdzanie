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
import pl.pozdro320.gui.holder.CheckerHolder;

public class CheckedGuiListener implements Listener {

    private final PozdroSprawdzanieMain plugin;

    public CheckedGuiListener(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CheckerHolder holder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player moderator)) return;

        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getInventory().getSize()) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        String action = meta.getPersistentDataContainer().get(plugin.getCheckerGUI().getActionKey(), PersistentDataType.STRING);
        if (action == null) return;

        Player target = Bukkit.getPlayer(holder.getTargetUuid());
        if (target == null || !target.isOnline()) {
            plugin.getConfigManager().getMessages().sendMessages(moderator, "errors.player-offline");
            moderator.closeInventory();
            return;
        }

        switch (action) {
            case "ACTION_CHECK":
                if (plugin.getCheckLocation() == null) {
                    plugin.getConfigManager().getMessages().sendMessages(moderator, "errors.no-checker");
                    return;
                }
                if (plugin.getSpawnLocation() == null) {
                    plugin.getConfigManager().getMessages().sendMessages(moderator, "errors.no-spawn");
                    return;
                }

                moderator.teleport(plugin.getCheckLocation());
                target.teleport(plugin.getCheckLocation());

                plugin.addChecked(target, moderator);

                plugin.getConfigManager().getMessages().sendMessages(target, "check-start-player", "{MODERATOR}", moderator.getName());
                plugin.getConfigManager().getMessages().sendMessages(moderator, "check-start-mod", "{PLAYER}", target.getName(), "{MODERATOR}", moderator.getName());
                moderator.closeInventory();
                break;

            case "ACTION_CLEAN":
                if (!plugin.isChecked(target)) {
                    plugin.getConfigManager().getMessages().sendMessages(moderator, "errors.is-checked", "{PLAYER}", target.getName());
                    moderator.closeInventory();
                    return;
                }

                moderator.closeInventory();
                target.teleport(plugin.getSpawnLocation());

                plugin.getConfigManager().getMessages().sendMessages(target, "player-clean", "{PLAYER}", target.getName(), "{MODERATOR}", moderator.getName());
                plugin.getConfigManager().getMessages().sendMessages(moderator, "mod-clean", "{PLAYER}", target.getName(), "{MODERATOR}", moderator.getName());

                String cleanReason = plugin.getConfig().getString("history-reasons.clean", "MARKED AS CLEAN");
                plugin.getHistoryManager().log(target.getName(), cleanReason, moderator.getName());
                plugin.removeChecked(target);
                break;

            case "ACTION_CHEATER":
                if (!plugin.isChecked(target)) {
                    plugin.getConfigManager().getMessages().sendMessages(moderator, "errors.is-checked", "{PLAYER}", target.getName());
                    moderator.closeInventory();
                    return;
                }

                moderator.closeInventory();
                plugin.getConfigManager().getMessages().sendMessages(moderator, "cheats-mod", "{PLAYER}", target.getName());

                String cheatsReason = plugin.getConfig().getString("history-reasons.cheats", "BANNED (CHEATS)");
                plugin.getHistoryManager().log(target.getName(), cheatsReason, moderator.getName());
                plugin.removeChecked(target);
                target.teleport(plugin.getSpawnLocation());

                plugin.getBansHelper().executeBan(target, moderator, "cheats-detected");
                break;

            case "ACTION_ADMISSION":
                if (!plugin.isChecked(target)) {
                    plugin.getConfigManager().getMessages().sendMessages(moderator, "errors.is-checked", "{PLAYER}", target.getName());
                    moderator.closeInventory();
                    return;
                }

                moderator.closeInventory();
                plugin.getConfigManager().getMessages().sendMessages(moderator, "admission-mod", "{PLAYER}", target.getName());

                String admissionReason = plugin.getConfig().getString("history-reasons.admission", "BANNED (ADMISSION)");
                plugin.getHistoryManager().log(target.getName(), admissionReason, moderator.getName());
                plugin.removeChecked(target);
                target.teleport(plugin.getSpawnLocation());

                plugin.getBansHelper().executeBan(target, moderator, "admission");
                break;

            case "ACTION_LACK_COOPERATION":
                if (!plugin.isChecked(target)) {
                    plugin.getConfigManager().getMessages().sendMessages(moderator, "errors.is-checked", "{PLAYER}", target.getName());
                    moderator.closeInventory();
                    return;
                }

                moderator.closeInventory();
                plugin.getConfigManager().getMessages().sendMessages(moderator, "lack-of-cooperation-mod", "{PLAYER}", target.getName());

                String noCoopReason = plugin.getConfig().getString("history-reasons.no-cooperation", "BANNED (LACK OF COOPERATION)");
                plugin.getHistoryManager().log(target.getName(), noCoopReason, moderator.getName());
                plugin.removeChecked(target);
                target.teleport(plugin.getSpawnLocation());

                plugin.getBansHelper().executeBan(target, moderator, "lack-of-cooperation");
                break;

            case "ACTION_TELEPORT":
                if (plugin.getCheckLocation() == null) {
                    plugin.getConfigManager().getMessages().sendMessages(moderator, "errors.no-checker");
                    return;
                }
                moderator.teleport(plugin.getCheckLocation());
                plugin.getConfigManager().getMessages().sendMessages(moderator, "tp-checker");
                moderator.closeInventory();
                break;

            case "ACTION_HISTORY":
                plugin.getHistoryGUI().openGUI(moderator, target);
                moderator.playSound(moderator.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
                break;
        }
    }
}