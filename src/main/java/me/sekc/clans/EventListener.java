package me.sekc.clans;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class EventListener implements Listener {
    Clans clans;

    EventListener(Clans clans) {
        this.clans = clans;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (!clans.databaseConnection.playerExists(playerUUID)) {
            Clans.log("Player " + event.getPlayer().getName() + " joined for the first time, registering them in the database.");
            clans.databaseConnection.createPlayer(playerUUID);
        }
    }
}
