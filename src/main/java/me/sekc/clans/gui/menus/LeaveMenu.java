package me.sekc.clans.gui.menus;

import com.mojang.brigadier.Command;
import me.sekc.clans.Clans;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.awt.*;
import java.util.Map;
import java.util.UUID;

public class LeaveMenu extends BaseMenu {
    public LeaveMenu(Clans clans) {
        super(clans);
    }

    @Override
    public String getConfigPath() {
        return "gui/leavemenu.yml";
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        if (clickedItem.id.equals("yes")) { // leave the clan
            MenuManager.closeInventory(e.getWhoClicked());

            UUID playerUUID = e.getWhoClicked().getUniqueId();
            String clanName = clans.databaseConnection.getPlayerClan(playerUUID);

            if (clanName.isEmpty()) {
                clans.messageInChat(e.getWhoClicked(), "commands.leave.not-in-clan", null);
                return;
            }

            if (clans.databaseConnection.getClanOwnedByPlayer(playerUUID) != null) {
                clans.messageInChat(e.getWhoClicked(), "commands.leave.owner-of-clan", null);
                return;
            }

            clans.databaseConnection.removePlayerFromClan(clanName, playerUUID);

            clans.messageInChat(e.getWhoClicked(), "commands.leave.left-clan",
                    Map.ofEntries(Map.entry("%clan_name%", clanName)));

            ((Player) e.getWhoClicked()).updateCommands(); // so any clan related commands from being in a clan disappear
        } else if (clickedItem.id.equals("no")) { // ignore
            MenuManager.closeInventory(e.getWhoClicked());
        }
    }
}
