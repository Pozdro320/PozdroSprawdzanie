package pl.pozdro320.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import pl.pozdro320.datacache.GuiItemData;
import pl.pozdro320.helper.MessageHelper;

import java.util.*;

public class GuiManager {

    private static final GuiItemData ERROR_ITEM = new GuiItemData(Material.BARRIER, "§cError", new ArrayList<>(), -1, 0);

    private final Map<String, GuiItemData> guiItems = new HashMap<>();
    private final Map<String, FileConfiguration> configs = new HashMap<>();

    public void clear() {
        this.guiItems.clear();
        this.configs.clear();
    }

    public void loadConfig(FileConfiguration config) {
        ConfigurationSection guiSection = config.getConfigurationSection("gui");
        if (guiSection == null) return;

        for (String guiName : guiSection.getKeys(false)) {
            configs.put(guiName, config);
            ConfigurationSection buttonsSection = config.getConfigurationSection("gui." + guiName + ".buttons");
            if (buttonsSection == null) continue;

            for (String buttonKey : buttonsSection.getKeys(false)) {
                String path = "gui." + guiName + ".buttons." + buttonKey;
                Material mat = Material.matchMaterial(config.getString(path + ".material", "STONE").toUpperCase());
                if (mat == null) mat = Material.BARRIER;

                String name = config.getString(path + ".name", "");
                List<String> lore = config.getStringList(path + ".lore");
                int slot = config.getInt(path + ".slot", -1);
                int modelData = config.getInt(path + ".model-data", 0);

                guiItems.put(guiName + "." + buttonKey, new GuiItemData(mat, name, lore, slot, modelData));
            }
        }
    }

    /**
     * @param inv Ekwipunek docelowy
     * @param guiName Nazwa GUI z configu
     * @param buttonKey Klucz przycisku (np. "back_button")
     * @param action Nazwa akcji do zapisu w PDC (np. "BACK_BUTTON")
     * @param typeKey NamespacedKey dla typu akcji
     * @param placeholders Mapa zamian, np. Map.of("%page%", "2")
     */
    public void setItem(Inventory inv, String guiName, String buttonKey, String action,
                        NamespacedKey typeKey, Map<String, String> placeholders) {

        GuiItemData data = getGuiItem(guiName, buttonKey);
        if (data.getSlot() < 0) return;

        ItemStack item = data.getBaseItem().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (placeholders.isEmpty()) {
                meta.setDisplayName(data.getColoredName());
                meta.setLore(data.getColoredLore());
            } else {
                String name = data.getRawName();
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    name = name.replace(entry.getKey(), entry.getValue());
                }
                meta.setDisplayName(MessageHelper.colored(name));

                if (!data.getRawLore().isEmpty()) {
                    List<String> lore = new ArrayList<>();
                    for (String line : data.getRawLore()) {
                        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                            line = line.replace(entry.getKey(), entry.getValue());
                        }
                        lore.add(MessageHelper.colored(line));
                    }
                    meta.setLore(lore);
                }
            }

            if (item.getType() == Material.PLAYER_HEAD && placeholders.containsKey("{PLAYER}")) {
                SkullMeta skullMeta = (SkullMeta) meta;
                String playerName = placeholders.get("{PLAYER}");

                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            }

            for (ItemFlag flag : ItemFlag.values()) meta.addItemFlags(flag);

            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, action);
            item.setItemMeta(meta);
        }
        inv.setItem(data.getSlot(), item);
    }

    public void setItem(Inventory inv, String guiName, String buttonKey, String action,
                        NamespacedKey typeKey, String... replacements) {
        Map<String, String> placeholderMap = new HashMap<>();
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                placeholderMap.put(replacements[i], replacements[i + 1]);
            }
        }
        setItem(inv, guiName, buttonKey, action, typeKey, placeholderMap);
    }

    /**
     * Przeciążona metoda  ItemStack
     * Zwraca gotowy przedmiot.
     */
    public ItemStack setItem(String guiName, String buttonKey, String action,
                            NamespacedKey typeKey, Map<String, String> placeholders) {

        GuiItemData data = getGuiItem(guiName, buttonKey);
        ItemStack item = data.getBaseItem().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (placeholders.isEmpty()) {
                meta.setDisplayName(data.getColoredName());
                meta.setLore(data.getColoredLore());
            } else {
                String name = data.getRawName();
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    name = name.replace(entry.getKey(), entry.getValue());
                }
                meta.setDisplayName(MessageHelper.colored(name));

                if (!data.getRawLore().isEmpty()) {
                    List<String> lore = new ArrayList<>();
                    for (String line : data.getRawLore()) {
                        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                            line = line.replace(entry.getKey(), entry.getValue());
                        }
                        lore.add(MessageHelper.colored(line));
                    }
                    meta.setLore(lore);
                }
            }

            if (item.getType() == Material.PLAYER_HEAD && placeholders.containsKey("{PLAYER}")) {
                SkullMeta skullMeta = (SkullMeta) meta;
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(placeholders.get("{PLAYER}")));
            }

            for (ItemFlag flag : ItemFlag.values()) meta.addItemFlags(flag);
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, action);

            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack setItem(String guiName, String buttonKey, String action,
                            NamespacedKey typeKey, String... replacements) {
        Map<String, String> placeholderMap = new HashMap<>();
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                placeholderMap.put(replacements[i], replacements[i + 1]);
            }
        }
        return setItem(guiName, buttonKey, action, typeKey, placeholderMap);
    }

    public GuiItemData getGuiItem(String guiName, String itemName) {
        GuiItemData data = guiItems.get(guiName + "." + itemName);
        return data != null ? data : ERROR_ITEM;
    }

    public String getGuiTitle(String guiName) {
        FileConfiguration config = configs.get(guiName);
        return config != null ? config.getString("gui." + guiName + ".title", "Menu") : "Menu";
    }

    public int getGuiSize(String guiName) {
        FileConfiguration config = configs.get(guiName);
        return config != null ? config.getInt("gui." + guiName + ".size", 54) : 54;
    }

    public Map<String, GuiItemData> getGuiItems() {
        return this.guiItems;
    }

    public Map<String, FileConfiguration> getConfigs() {
        return configs;
    }
}