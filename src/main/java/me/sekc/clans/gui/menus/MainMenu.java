package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MainMenu extends BaseMenu {
    public MainMenu(Clans clans) {
        super(clans);
    }

    @Override
    public String getConfigPath() {
        return "gui/mainmenu.yml";
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        if (clickedItem.id.equals("storage")) {
            MenuManager.open(e.getWhoClicked(), new StorageMenu(clans));
        }
        if (clickedItem.id.equals("leave")) {
            MenuManager.open(e.getWhoClicked(), new LeaveMenu(clans));
        }
    }
}
