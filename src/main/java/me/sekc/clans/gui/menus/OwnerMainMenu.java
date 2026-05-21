package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.event.inventory.InventoryClickEvent;

public class OwnerMainMenu extends MainMenu {
    public OwnerMainMenu(Clans clans) {
        super(clans);
    }

    @Override
    public String getConfigPath() {
        return "gui/mainmenu_owner.yml";
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedItem, e); // Handle the normal MainMenu buttons

        if (clickedItem.id != null) {
            if (clickedItem.id.equals("delete")) {
                MenuManager.open(e.getWhoClicked(), new DeleteMenu(clans));
            }
            if (clickedItem.id.equals("manage")) {
                clans.messageInChat(e.getWhoClicked(), "todo", null);
            }
        }
    }
}
