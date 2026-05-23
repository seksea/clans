package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ManageClanMenu extends BaseMenu {
	String clanName;

    public ManageClanMenu(Clans clans, String clanName) {
        super(clans);
		this.clanName = clanName;
    }

    @Override
    public String getConfigPath() {
        return "gui/manageclanmenu.yml";
    }

    @Override
    public void fillContent(Player player, Inventory gui) {
        super.fillContent(player, gui);

        if (clanName == null) {
            throw new RuntimeException("Player tried to get storage contents when not in clan");
        }

        int curIndex = 0;
        for (LayoutItem item : layoutArray) {
			if (item != null) {
				if (item.id.equals("description")) {
					item.lore = "<grey>"+clans.databaseConnection.getClanDescription(clanName)+"</grey>";
					gui.setItem(curIndex, item.getItemStack());
				}
			}

            curIndex++;
        }
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedLayoutItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedLayoutItem, e);

        if (clickedLayoutItem.id != null) {
            if (clickedLayoutItem.id.equals("description")) {
                clans.messageInChat(e.getWhoClicked(), "manage.awaiting-description-input", null);

                MenuManager.closeInventory(e.getWhoClicked());

                MenuManager.performActionAfterTyping(e.getWhoClicked().getUniqueId(), message -> {
                    if (message.equals("cancel")) {
                        clans.messageInChat(e.getWhoClicked(), "cancelled", null);
                        return;
                    }

					clans.databaseConnection.setClanDescription(clanName, message);

					clans.messageInChat(e.getWhoClicked(), "manage.update-var",
						Map.ofEntries(
							Map.entry("%var_name%", "description"),
							Map.entry("%clan_name%", clanName),
							Map.entry("%new_value%", message)
						)
					);

                    MenuManager.open(e.getWhoClicked(), new ManageClanMenu(clans, this.clanName)); // re-open this menu
                });
            } else if (clickedLayoutItem.id.equals("players")) {
				clans.messageInChat(e.getWhoClicked(), "todo", null);
			}
        }
    }
}
