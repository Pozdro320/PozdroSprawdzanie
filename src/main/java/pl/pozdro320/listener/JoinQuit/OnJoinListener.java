package pl.pozdro320.listener.JoinQuit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import pl.pozdro320.PozdroSprawdzanieMain;
import pl.pozdro320.helper.MessageHelper;

public class OnJoinListener implements Listener {

    private final PozdroSprawdzanieMain plugin;

    public OnJoinListener(PozdroSprawdzanieMain plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getVisibilityHelper().handleJoin(player);

        if (player.hasPermission("pozdrosprawdzanie.admin") && plugin.isUpdateAvailable()) {

            MessageHelper.build("")
                    .send(player);
            MessageHelper.build(" &8» &fWykryto nową wersję pluginu &dPozdroSprawdzanie&f!")
                    .send(player);
            MessageHelper.build(" &8» &fPobierz ją na &bhttps://github.com/Pozdro320/PozdroSprawdzanie")
                    .send(player);
            MessageHelper.build("")
                    .send(player);

            MessageHelper.sendBar(player, "&8» &fDostępna jest nowa wersja &dpozdrosprawdzanie&f!");
        }
    }
}
