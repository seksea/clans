package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FurnaceMenu extends BaseMenu {
	String clanName;
	List<ItemStack> itemsInFurnace = new ArrayList<>();

	LayoutItem burnItem; // so the lore can be quickly edited
	int burnItemSlot;

    public FurnaceMenu(Clans clans, String clanName) {
        super(clans);
		this.clanName = clanName;
    }

    @Override
    public String getConfigPath() {
        return "gui/furnacemenu.yml";
    }

	@Override
	public void fillContent(Player player, Inventory gui) {
		super.fillContent(player, gui);

		itemsInFurnace.clear();
		int index = 0;
		for (LayoutItem lItem : layoutArray) {
			if (lItem == null) {
				index++;
				continue;
			}

			if (lItem.custom) // initialise itemsInFurnace
				itemsInFurnace.add(ItemStack.empty());

			if (lItem.id != null) {
				if (lItem.id.equals("itemofday")) {
					lItem.material = Material.SPRUCE_LOG; // fake

					lItem.lore = clans.getMessageWithPlaceholders(player.getUniqueId(), "furnace.itemofdaylore", Map.ofEntries(
						Map.entry("%item_of_day%", "Spruce Log"),
						Map.entry("%num_xp_normal%", Integer.toString(2)),
						Map.entry("%multiplier%", Integer.toString(10)), // fake data for now TODO
						Map.entry("%num_xp%", Integer.toString(20))
					));
				}
				if (lItem.id.equals("burn")) {
					lItem.lore = clans.getMessageWithPlaceholders(player.getUniqueId(), "furnace.burnlore", Map.ofEntries(
						Map.entry("%furnace_worth%", Integer.toString(0))
					));
					burnItem = lItem;
					burnItemSlot = index;
				}
			}

			gui.setItem(index, lItem.getItemStack());
			index++;
		}
	}

	public int getXPForAllItemsInFurnace() {
		int numXP = 0;
		for (ItemStack item : itemsInFurnace) {
			DatabaseConnection.FurnaceItem furnaceItem = clans.databaseConnection.getSimilarItemFromFurnaceItemList(item);
			if (furnaceItem != null)
				numXP += item.getAmount() * furnaceItem.xp;
		}
		return numXP;
	}

    @Override
    protected void layoutItemClicked(LayoutItem clickedLayoutItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedLayoutItem, e);

        // handle putting and taking items from this inventory (only "_" chars in the gui yml) and keep in sync with the database
        super.handleStorageClicked(clickedLayoutItem, e, (itemStack, customSlotID) -> {

			if (!itemStack.isEmpty()) {
				DatabaseConnection.FurnaceItem furnaceItem = clans.databaseConnection.getSimilarItemFromFurnaceItemList(itemStack);

				if (furnaceItem == null) {
					// not in list, cancel and do nothing
					e.setCancelled(true);
					return;
				}
			}

			// set to air or similar item, we can move this into furnace
			itemsInFurnace.set(customSlotID, itemStack);

			burnItem.lore = clans.getMessageWithPlaceholders(e.getWhoClicked().getUniqueId(), "furnace.burnlore", Map.ofEntries(
				Map.entry("%furnace_worth%", Integer.toString(getXPForAllItemsInFurnace()))
			));
			e.getClickedInventory().setItem(burnItemSlot, burnItem.getItemStack());
        });

        if (clickedLayoutItem.id != null) {
            if (clickedLayoutItem.id.equals("burn")) {
				UUID playerUUID = e.getWhoClicked().getUniqueId();
				int clanXPToAdd = getXPForAllItemsInFurnace();
				int newClanXP = clans.databaseConnection.getClanExperience(clanName) + clanXPToAdd;
				int playerXPToAdd = (int)Math.floor((double)clanXPToAdd * clans.getConfig().getDouble("leveling.player-xp-mul"));
				int playerXP = clans.databaseConnection.getPlayerExperience(playerUUID) + playerXPToAdd;

				itemsInFurnace.clear();
				int index = 0;
				for (LayoutItem lItem : layoutArray) {
					if (lItem == null) {
						index++;
						continue;
					}

					if (lItem.custom) { // clear itemsInFurnace as we have burned everything
						itemsInFurnace.add(ItemStack.empty());
						e.getClickedInventory().setItem(index, ItemStack.empty());
					}

					index++;
				}

				clans.databaseConnection.setClanExperience(clans, clanName, newClanXP);
				clans.databaseConnection.setPlayerExperience(playerUUID, playerXP);

				clans.messageInChat(e.getWhoClicked(), "furnace.burned-items", Map.ofEntries(
					Map.entry("%num_clan_xp%", Integer.toString(clanXPToAdd)),
					Map.entry("%num_player_xp%", Integer.toString(playerXPToAdd))
				));

            } else if (clickedLayoutItem.id.equals("itemlist")) {
				clans.messageInChat(e.getWhoClicked(), "todo", null);
            }
        }
    }

	@Override
	public void handleClose(InventoryCloseEvent e) {
		super.handleClose(e);
		// return items to player

		for (ItemStack item : itemsInFurnace) {
			// return items in furnace back to inventory
			e.getView().getBottomInventory().addItem(item);
		}
	}
}
