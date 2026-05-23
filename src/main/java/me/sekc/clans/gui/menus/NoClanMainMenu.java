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
                    try {
                        player.performCommand("clan new " + message.replace(" ", "_")); // perform `/clan new` command
                    } catch (Exception err) {
                        clans.messageInChat(e.getWhoClicked(), "new.invalid-name", null);
                    }
                });
			} else if (clickedItem.id.equals("leaderboard")) {
				MenuManager.open(e.getWhoClicked(), new LeaderboardMenu(clans));
            } else if (clickedItem.id.equals("invites")) {
                MenuManager.open(e.getWhoClicked(), new InvitesMenu(clans));
            }
        }
    }
}
