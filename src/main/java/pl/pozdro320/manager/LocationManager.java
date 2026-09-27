package pl.pozdro320.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import pl.pozdro320.PozdroSprawdzanieMain;

public class LocationManager {

    private static final String PATH_PREFIX = "locations.";
    private final PozdroSprawdzanieMain plugin;

    public LocationManager(PozdroSprawdzanieMain plugin) {
        this.plugin = plugin;
    }

    public void save(String name, Location loc) {
        if (loc == null || loc.getWorld() == null) return;

        FileConfiguration config = plugin.getConfigManager().getConfig();
        String path = PATH_PREFIX + name;

        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", (float) loc.getYaw());
        config.set(path + ".pitch", (float) loc.getPitch());

        plugin.saveConfig();
    }

    public Location load(String name) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String path = PATH_PREFIX + name;

        if (!config.contains(path)) return null;

        String worldName = config.getString(path + ".world");
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        return new Location(
                world,
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
    }
}