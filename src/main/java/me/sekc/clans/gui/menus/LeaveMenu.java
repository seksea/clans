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
        super.layoutItemClicked(clickedItem, e);

        if (clickedItem.id.equals("yes")) { // leave the clan
            MenuManager.closeInventory(e.getWhoClicked());

            if (clans.databaseConnection.getClanOwnedByPlayer(e.getWhoClicked().getUniqueId()) != null) {
                clans.messageInChat(e.getWhoClicked(), "commands.leave.owner-of-clan", null);
                return;
            }

            ((Player)e.getWhoClicked()).performCommand("clan leave yes_i_am_sure"); // perform the command
        } else if (clickedItem.id.equals("no")) { // ignore
            MenuManager.closeInventory(e.getWhoClicked());
        }
    }
}
