package pl.pozdro320.gui.holder;

import org.bukkit.inventory.InventoryHolder;
import java.util.UUID;

public interface SprawdzanieGuiHolder extends InventoryHolder {
    UUID getTargetUuid();
    String getTargetName();
}