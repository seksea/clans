package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.gui.BaseMenu;
import org.bukkit.event.inventory.InventoryClickEvent;

public class StorageMenu extends BaseMenu {
    public StorageMenu(Clans clans) {
        super(clans);
    }

    @Override
    public String getConfigPath() {
        return "gui/storagemenu.yml";
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        Clans.log(clickedItem.id);
    }
}
