package pl.pozdro320.gui.holder;

import lombok.Getter;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

@Getter
public class HistoryHolder implements SprawdzanieGuiHolder {

    private final UUID targetUuid;
    private final String targetName;
    private Inventory inventory;

    public HistoryHolder(UUID targetUuid, String targetName) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}