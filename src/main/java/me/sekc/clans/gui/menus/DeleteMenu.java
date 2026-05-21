package me.sekc.clans.gui.menus;

import com.mojang.brigadier.Command;
import me.sekc.clans.Clans;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;
import java.util.UUID;

public class DeleteMenu extends BaseMenu {
    public DeleteMenu(Clans clans) {
        super(clans);
    }

    @Override
    public String getConfigPath() {
        return "gui/deletemenu.yml";
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        if (clickedItem.id.equals("yes")) { // delete the clan
            MenuManager.closeInventory(e.getWhoClicked());

            ((Player)e.getWhoClicked()).performCommand("clan delete yes_i_am_sure"); // perform the command
        } else if (clickedItem.id.equals("no")) { // ignore
            MenuManager.closeInventory(e.getWhoClicked());
        }
    }
}
