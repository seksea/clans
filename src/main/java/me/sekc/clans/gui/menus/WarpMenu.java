package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WarpMenu extends BaseMenu {
	String clanName; // we need to keep clan name here so we can view other clans storage as an admin
	public WarpMenu(Clans clans, String clanName) {
		super(clans);
		this.clanName = clanName;
	}

    @Override
    public String getConfigPath() {
        return "gui/warpmenu.yml";
    }

    @Override
    public void fillContent(Player player, Inventory gui) {
        super.fillContent(player, gui);

        if (clanName == null) {
            throw new RuntimeException("Player tried to open warp menu when not in clan");
        }

        List<DatabaseConnection.WarpSlot> warpSlots = clans.databaseConnection.getWarpListFromClan(clanName);

        int curIndex = 0;
        for (LayoutItem item : layoutArray) {
            // List the storages we have
            if (item != null && item.custom) {
                int customSlotId = this.slotIdToCustomSlotID(curIndex);

                if (customSlotId < warpSlots.size()) {
                    DatabaseConnection.WarpSlot slot = warpSlots.get(customSlotId);
                    item.id = "slot " + String.valueOf(customSlotId);
                    item.material = Material.valueOf(slot.color.name() + "_DYE");
                    item.name = slot.title;
					item.lore = clans.getMessageWithPlaceholders(player.getUniqueId(), "warps.warp-lore", null);

                    gui.setItem(curIndex, item.getItemStack());
                }
            };

            curIndex++;
        }
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedItem, e);

        if (clickedItem.id != null) {
            if (clickedItem.id.startsWith("slot ")) {
				int slotId = Integer.valueOf(clickedItem.id.split(" ")[1]);

				if (e.isRightClick()) {
					MenuManager.open(e.getWhoClicked(), new WarpEditMenu(clans, slotId, clanName));
				} else {
					DatabaseConnection.WarpSlot warpSlot = clans.databaseConnection.getWarpFromClan(clanName, slotId);
					// teleport to warp
					try {
						e.getWhoClicked().teleport(warpSlot.position);
					} catch (Exception exception) {
						clans.messageInChat(e.getWhoClicked(), "warps.failed", null);
					}
				}
            }
        }
    }
}
