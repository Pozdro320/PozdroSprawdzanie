package pl.pozdro320.gui.holder;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

@Getter
public class HistoryHolder implements SprawdzanieGuiHolder {

    private final UUID targetUuid;
    private final String targetName;
    @Setter
    private int page;
    private Inventory inventory;

    public HistoryHolder(UUID targetUuid, String targetName, int page) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.page = page;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}