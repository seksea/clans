package me.sekc.clans.gui;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.sekc.clans.Clans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuManager {
    static Map<UUID, BaseMenu> playerToOpenGUIMap = new HashMap<>();

    static public void open(CommandSourceStack commandSource, BaseMenu menu) {
        if (commandSource.getExecutor() instanceof Player player) {
            playerToOpenGUIMap.put(player.getUniqueId(), menu);

            Inventory gui = Bukkit.getServer().createInventory(player, 9*6, Component.text(menu.getTitle()).color(TextColor.color(255, 255, 255)));

            menu.fillContent(gui);

            player.openInventory(gui);
        } else {
            Clans.warn("tried to open GUI for non-player.");
        }
    }

    static public void onClick(InventoryClickEvent e) {
        BaseMenu openGUI = playerToOpenGUIMap.get(e.getWhoClicked().getUniqueId());

        if (openGUI != null) {
            openGUI.itemClicked(e);
        }
    }

    static public void onInventoryClose(InventoryCloseEvent e) {
        playerToOpenGUIMap.remove(e.getPlayer().getUniqueId()); // no UI open anymore
    }
}
