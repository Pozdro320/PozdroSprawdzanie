package pl.pozdro320.datacache;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;
import lombok.Getter;
import pl.pozdro320.helper.MessageHelper;

@Getter
public class GuiItemData {

private final ItemStack baseItem;
    private final String rawName;
    private final List<String> rawLore;
    private final String coloredName;
    private final List<String> coloredLore;
    private final int slot;
    private final int modeldata;

    private static final ItemFlag[] FLAGS = ItemFlag.values();

    public GuiItemData(Material material, String name, List<String> lore, int slot, int modeldata) {
        this.baseItem = new ItemStack(material != null ? material : Material.BARRIER);
        this.rawName = name != null ? name : "";
        this.rawLore = lore != null ? lore : new ArrayList<>();
        this.slot = slot;
        this.modeldata = modeldata;

        this.coloredName = MessageHelper.colored(this.rawName);
        this.coloredLore = this.rawLore.stream()
                .map(MessageHelper::colored)
                .collect(Collectors.toList());

        ItemMeta meta = this.baseItem.getItemMeta();
        if (meta != null) {
            if (this.modeldata != 0) {
                meta.setCustomModelData(this.modeldata);
            }
            meta.addItemFlags(FLAGS);
            this.baseItem.setItemMeta(meta);
        }
    }

    public ItemStack createIcon() {
        ItemStack item = this.baseItem.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(this.coloredName);
            meta.setLore(this.coloredLore);
            item.setItemMeta(meta);
        }
        return item;
    }
}