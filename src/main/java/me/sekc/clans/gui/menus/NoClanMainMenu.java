package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class NoClanMainMenu extends BaseMenu {
    public NoClanMainMenu(Clans clans) {
        super(clans);
    }

    @Override
    public String getConfigPath() {
        return "gui/mainmenu_noclan.yml";
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedItem, e);

        Player player = (Player)e.getWhoClicked();

        if (clickedItem.id != null) {
            if (clickedItem.id.equals("new")) {
                clans.messageInChat(e.getWhoClicked(), "new.awaiting-input", null);

                MenuManager.closeInventory(player);

                MenuManager.performActionAfterTyping(player.getUniqueId(), message -> {
                    player.performCommand("clan new " + message.replace(" ", "_")); // perform `/clan new` command
                });
            } else if (clickedItem.id.equals("invites")) {

            }
        }
    }
}
