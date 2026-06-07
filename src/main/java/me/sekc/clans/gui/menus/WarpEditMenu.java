package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class WarpEditMenu extends BaseMenu {
    int index = 0; // The index in the database
	String clanName;

    public WarpEditMenu(Clans clans, int index, String clanName) {
        super(clans);
        this.index = index;
		this.clanName = clanName;
    }

    @Override
    public String getConfigPath() {
        return "gui/warpeditmenu.yml";
    }

    @Override
    public void fillContent(Player player, Inventory gui) {
        super.fillContent(player, gui);

        if (clanName == null) {
            throw new RuntimeException("Player tried to get warp menu when not in clan");
        }
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedLayoutItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedLayoutItem, e);

        if (clickedLayoutItem.id != null) {
            if (clickedLayoutItem.id.equals("back")) {
                MenuManager.open(e.getWhoClicked(), new WarpMenu(clans, clanName));
            } else if (clickedLayoutItem.id.equals("rename")) {
                clans.messageInChat(e.getWhoClicked(), "warps.awaiting-name-input", null);

                MenuManager.closeInventory(e.getWhoClicked());

                MenuManager.performActionAfterTyping(e.getWhoClicked().getUniqueId(), message -> {
                    if (message.equals("cancel")) {
                        clans.messageInChat(e.getWhoClicked(), "cancelled", null);
						MenuManager.open(e.getWhoClicked(), new WarpEditMenu(clans, this.index, this.clanName)); // re-open this menu
                    } else {
						clans.databaseConnection.editWarpName(clanName, this.index, message);
						clans.messageInChat(e.getWhoClicked(), "warps.renamed", Map.ofEntries(Map.entry("%new_name%", message)));
						MenuManager.open(e.getWhoClicked(), new WarpEditMenu(clans, this.index, this.clanName)); // re-open this menu
					}
                });
            } else if (clickedLayoutItem.id.equals("setpos")) {
				Location newPos = e.getWhoClicked().getLocation();
				clans.messageInChat(e.getWhoClicked(), "warps.setwarp", Map.ofEntries(
					Map.entry("%pos_x%", String.valueOf(newPos.getBlockX())),
					Map.entry("%pos_y%", String.valueOf(newPos.getBlockY())),
					Map.entry("%pos_z%", String.valueOf(newPos.getBlockZ()))
				));

				clans.databaseConnection.setWarpPosition(clanName, this.index, newPos);
			} else if (clickedLayoutItem.id.equals("color")) {
                clans.messageInChat(e.getWhoClicked(), "warps.awaiting-color-input", null);

                MenuManager.closeInventory(e.getWhoClicked());

                MenuManager.performActionAfterTyping(e.getWhoClicked().getUniqueId(), message -> {
                    if (message.equals("cancel")) {
                        clans.messageInChat(e.getWhoClicked(), "cancelled", null);
						MenuManager.open(e.getWhoClicked(), new WarpEditMenu(clans, this.index, clanName)); // re-open this menu
                    } else {
						try {
							clans.databaseConnection.editWarpColor(clanName, this.index, DyeColor.valueOf(message.toUpperCase()));
							clans.messageInChat(e.getWhoClicked(), "warps.recolored", Map.ofEntries(Map.entry("%new_color%", message)));
						} catch (Exception ex) {
							clans.messageInChat(e.getWhoClicked(), "warps.couldnt-recolor", Map.ofEntries(Map.entry("%new_color%", message)));
						}
						MenuManager.open(e.getWhoClicked(), new WarpEditMenu(clans, this.index, clanName)); // re-open this menu
					}
                });
            }
        }
    }
}
