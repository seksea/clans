package me.sekc.clans;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent e) {
        MenuManager.onClick(e);
    }

    @EventHandler
    public void OnInventoryClose(InventoryCloseEvent e) {
        MenuManager.onInventoryClose(e);
    }

    @EventHandler
    public void OnPlayer(AsyncChatEvent e) {
        MenuManager.onChat(e);
    }
}
