package me.sekc.clans.gui.menus;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.registry.keys.DataComponentTypeKeys;
import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StorageContentsMenu extends BaseMenu {
    int index = 0; // The index in the database
	String clanName;

    public StorageContentsMenu(Clans clans, int index, String clanName) {
        super(clans);
        this.index = index;
		this.clanName = clanName;
    }

    @Override
    public String getConfigPath() {
        return "gui/storagemenu_contents.yml";
    }

    @Override
    public void fillContent(Player player, Inventory gui) {
        super.fillContent(player, gui);

        if (clanName == null) {
            throw new RuntimeException("Player tried to get storage contents when not in clan");
        }

        DatabaseConnection.StorageSlot storage = clans.databaseConnection.getStorageFromClan(clanName, index);

        int curIndex = 0;
        for (LayoutItem item : layoutArray) {
            // List the contents of this storage
            if (curIndex > storage.itemStacks.size()) break; // No more is stored

            if (item != null && item.custom) { // only modify items in the layout that are custom
                ItemStack itemStack = storage.itemStacks.get(curIndex);

                if (!itemStack.isEmpty()) {
                    item.id = "slot " + String.valueOf(curIndex);
                    item.customItemStack = itemStack;

                    gui.setItem(curIndex, itemStack);
                }
            }

            curIndex++;
        }
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedLayoutItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedLayoutItem, e);

        ItemStack clickedItemStack = e.getCurrentItem();

        String clanName = clans.databaseConnection.getPlayerClan(e.getWhoClicked().getUniqueId());

        // handle putting and taking items from this inventory (only "_" chars in the gui yml) and keep in sync with the database
        super.handleStorageClicked(clickedLayoutItem, e, (itemStack, customSlotID) -> {
            clans.databaseConnection.setItemInClanStorage(clanName, itemStack, this.index, customSlotID);
        });

        if (clickedLayoutItem.id != null) {
            if (clickedLayoutItem.id.equals("back")) {
                MenuManager.open(e.getWhoClicked(), new StorageMenu(clans, clanName));
            } else if (clickedLayoutItem.id.equals("rename")) {
                clans.messageInChat(e.getWhoClicked(), "storage.awaiting-name-input", null);

                MenuManager.closeInventory(e.getWhoClicked());

                MenuManager.performActionAfterTyping(e.getWhoClicked().getUniqueId(), message -> {
                    if (message.equals("cancel")) {
                        clans.messageInChat(e.getWhoClicked(), "cancelled", null);
                        return;
                    }
                    clans.databaseConnection.editStorageName(clanName, this.index, message);
                    clans.messageInChat(e.getWhoClicked(), "storage.renamed", Map.ofEntries(Map.entry("%new_name%", message)));
                    MenuManager.open(e.getWhoClicked(), new StorageContentsMenu(clans, this.index, this.clanName)); // re-open this menu
                });
            } else if (clickedLayoutItem.id.equals("color")) {
                clans.messageInChat(e.getWhoClicked(), "storage.awaiting-color-input", null);

                MenuManager.closeInventory(e.getWhoClicked());

                MenuManager.performActionAfterTyping(e.getWhoClicked().getUniqueId(), message -> {
                    if (message.equals("cancel")) {
                        clans.messageInChat(e.getWhoClicked(), "cancelled", null);
                        return;
                    }
                    try {
                        clans.databaseConnection.editStorageColor(clanName, this.index, DyeColor.valueOf(message.toUpperCase()));
                        clans.messageInChat(e.getWhoClicked(), "storage.recolored", Map.ofEntries(Map.entry("%new_color%", message)));
                    } catch (Exception ex) {
                        clans.messageInChat(e.getWhoClicked(), "storage.couldnt-recolor", Map.ofEntries(Map.entry("%new_color%", message)));
                    }
                    MenuManager.open(e.getWhoClicked(), new StorageContentsMenu(clans, this.index, clanName)); // re-open this menu
                });
            }
        }
    }
}
