package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.*;

public class FurnaceMenu extends BaseMenu {
	String clanName;

	public static Set<String> furnaceMenuOpenedForClan = new HashSet<>(); // Used so that only one player can access furnace at once to prevent dupes

	public FurnaceMenu(Clans clans, String clanName) {
		super(clans);
		this.clanName = clanName;

		furnaceMenuOpenedForClan.add(clanName);
	}

	@Override
	public String getConfigPath() {
		return "gui/furnacemenu.yml";
	}

	@Override
	public void fillContent(Player player, Inventory gui) {
		super.fillContent(player, gui);

		if (clanName == null) {
			throw new RuntimeException("Player tried to open furnace when not in clan");
		}

		List<ItemStack> furnaceItems = clans.databaseConnection.getFurnaceFromClan(clanName);

		int curIndex = 0;
		int customSlotIndex = 0;
		for (LayoutItem item : layoutArray) {
			// List the contents of this storage

			if (item != null) {
				if (item.id != null) {
					if (item.id.equals("itemofday")) {
						item.material = Material.SPRUCE_LOG; // fake

						item.lore = clans.getMessageWithPlaceholders(player.getUniqueId(), "furnace.itemofdaylore", Map.ofEntries(
							Map.entry("%item_of_day%", "Spruce Log"),
							Map.entry("%num_xp_normal%", Integer.toString(2)),
							Map.entry("%multiplier%", Integer.toString(10)), // fake data for now TODO
							Map.entry("%num_xp%", Integer.toString(20))
						));

						gui.setItem(curIndex, item.getItemStack());
					}
					if (item.id.equals("info")) {
						int ticksToBurnAll = numCustomSlots * 64 * clans.getConfig().getInt("furnace.delayTicks");
						item.lore = clans.getMessageWithPlaceholders(player.getUniqueId(), "furnace.infolore", Map.ofEntries(
							Map.entry("%time_to_burn_all%", Duration.ofSeconds((long)((float)ticksToBurnAll / Bukkit.getServer().getServerTickManager().getTickRate())).toString().substring(2).toLowerCase())
						));

						gui.setItem(curIndex, item.getItemStack());
					}
				}

				if (item.custom && furnaceItems != null && customSlotIndex <= furnaceItems.size()) { // only modify items in the layout that are custom

					ItemStack itemStack = furnaceItems.get(customSlotIndex);

					if (!itemStack.isEmpty()) {
						item.id = "slot " + customSlotIndex;
						item.customItemStack = itemStack;

						gui.setItem(curIndex, itemStack);
					}

					customSlotIndex++;
				}
			}
			curIndex++;
		}
	}

	@Override
	protected void layoutItemClicked(LayoutItem clickedLayoutItem, InventoryClickEvent e) {
		super.layoutItemClicked(clickedLayoutItem, e);

		String clanName = clans.databaseConnection.getPlayerClan(e.getWhoClicked().getUniqueId());

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

			clans.databaseConnection.setItemInClanFurnace(clanName, itemStack, customSlotID);
		});

		if (clickedLayoutItem.id != null) {
			if (clickedLayoutItem.id.equals("itemlist")) {
				MenuManager.open(e.getWhoClicked(), new FurnaceItemListMenu(clans, 0));
			}
		}
	}

	@Override
	public void handleClose(InventoryCloseEvent e) {
		furnaceMenuOpenedForClan.remove(clanName);
	}
}