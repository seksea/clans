package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FurnaceItemListMenu extends BaseMenu {
	int pageIndex;
	boolean isLastPage = false;

    public FurnaceItemListMenu(Clans clans, int pageIndex) {
        super(clans);
		this.pageIndex = pageIndex;
    }

    @Override
    public String getConfigPath() {
        return "gui/furnacemenu_itemlist.yml";
    }

	@Override
	public void fillContent(Player player, Inventory gui) {
		super.fillContent(player, gui);

		int index = 0;
		int customIndex = 0;
		List<DatabaseConnection.FurnaceItem> pageData = clans.databaseConnection.getPageOfFurnaceItems(pageIndex*numCustomSlots, numCustomSlots);
		for (LayoutItem lItem : layoutArray) {
			if (lItem == null) {
				index++;
				continue;
			}

			if (lItem.custom)  {
				if (customIndex >= pageData.size()) {
					this.isLastPage = true; // hit end
					break;
				}

				DatabaseConnection.FurnaceItem furnaceItem = pageData.get(customIndex);

				lItem.customItemStack = furnaceItem.item;

				ItemMeta meta = lItem.customItemStack.getItemMeta();

				int playerXPToAdd = (int)Math.floor((double)furnaceItem.xp * clans.getConfig().getDouble("leveling.player-xp-mul"));

				String lore = clans.getMessageWithPlaceholders(player.getUniqueId(), "furnace.item-list.lore", Map.ofEntries(
					Map.entry("%num_xp%", Integer.toString(furnaceItem.xp)),
					Map.entry("%num_player_xp%", Integer.toString(playerXPToAdd))
				));
				List<Component> loreList = new ArrayList<>();
				for (String loreLine : lore.split("\\n")) {
					loreList.add(MiniMessage.miniMessage().deserialize(loreLine));
				}
				meta.lore(loreList);

				lItem.customItemStack.setItemMeta(meta);

				gui.setItem(index, lItem.getItemStack());
				customIndex++;
			}

			index++;
		}
	}


    @Override
    protected void layoutItemClicked(LayoutItem clickedLayoutItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedLayoutItem, e);

        if (clickedLayoutItem.id != null) {
			if (clickedLayoutItem.id.equals("prevpage")) {
				if (this.pageIndex > 0) {
					MenuManager.open(e.getWhoClicked(), new FurnaceItemListMenu(clans, pageIndex-1));
				}
			} else if (clickedLayoutItem.id.equals("nextpage")) {
				if (!this.isLastPage) {
					MenuManager.open(e.getWhoClicked(), new FurnaceItemListMenu(clans, pageIndex+1));
				}
			} else if (clickedLayoutItem.id.equals("back")) {
				String playerClan = clans.databaseConnection.getPlayerClan(e.getWhoClicked().getUniqueId());
				MenuManager.open(e.getWhoClicked(), new FurnaceMenu(clans, playerClan));
			}
        }
    }
}
