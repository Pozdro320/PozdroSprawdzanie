package pl.pozdro320.utils;

import java.lang.reflect.Field;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class RegistryUtil {
    private final Plugin plugin;

    public RegistryUtil(Plugin plugin) {
        this.plugin = plugin;
    }

    public RegistryUtil registerListeners(Listener... listeners){
        for(Listener listener : listeners){
            this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
        }
        return this;
    }

    public RegistryUtil registerCommands(Command... commands){
        SimpleCommandMap simpleCommandMap = null;
        try{
            Field field = plugin.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            simpleCommandMap = (SimpleCommandMap) field.get(plugin.getServer());
        }catch (Exception ignored){}

        if(simpleCommandMap != null)
            for(Command command : commands)
                simpleCommandMap.register("pozdro320", command);

        return this;
    }
}
