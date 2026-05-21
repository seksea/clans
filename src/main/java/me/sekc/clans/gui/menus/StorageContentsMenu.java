package me.sekc.clans.gui.menus;

import de.tr7zw.nbtapi.NBTItem;
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

public class StorageContentsMenu extends BaseMenu {
    int index = 0; // The index in the database

    public StorageContentsMenu(Clans clans, int index) {
        super(clans);
        this.index = index;
    }

    @Override
    public String getConfigPath() {
        return "gui/storagemenu_contents.yml";
    }

    @Override
    public void fillContent(Player player, Inventory gui) {
        super.fillContent(player, gui);

        String clanName = clans.databaseConnection.getPlayerClan(player.getUniqueId());

        if (clanName == null) {
            throw new RuntimeException("Player tried to get storage contents when not in clan");
        }

        DatabaseConnection.StorageSlot storage = clans.databaseConnection.getStorageFromClan(clanName, index);

        int curIndex = 0;
        for (LayoutItem item : layoutArray) {
            // List the contents of this storage
            if (curIndex >= storage.nbtData.size()) break; // No more is stored

            if (item.custom) { // only modify items in the layout that are custom
                NBTItem nbtItem = storage.nbtData.get(curIndex);

                if (nbtItem != null) {
                    ItemStack itemStack = nbtItem.getItem();

                    item.id = "slot_" + String.valueOf(curIndex);
                    item.material = itemStack.getType();
                    item.name = String.valueOf(itemStack.displayName());

                    gui.setItem(curIndex, item.getItemStack());
                }
            }

            curIndex++;
        }
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedItem, e);

        String clanName = clans.databaseConnection.getPlayerClan(e.getWhoClicked().getUniqueId());

        if (clickedItem.id.equals("back")) {
            MenuManager.open(e.getWhoClicked(), new StorageMenu(clans));
        } else if (clickedItem.id.equals("rename")) {
            clans.messageInChat(e.getWhoClicked(), "commands.storage.awaiting-name-input", null);

            MenuManager.closeInventory(e.getWhoClicked());

            MenuManager.performActionAfterTyping(e.getWhoClicked().getUniqueId(), message -> {
                clans.databaseConnection.editStorageName(clanName, this.index, message);
                clans.messageInChat(e.getWhoClicked(), "commands.storage.renamed", Map.ofEntries(Map.entry("%new_name%", message)));
                MenuManager.open(e.getWhoClicked(), new StorageContentsMenu(clans, this.index)); // re-open this menu
            });
        } else if (clickedItem.id.equals("color")) {
            clans.messageInChat(e.getWhoClicked(), "commands.storage.awaiting-color-input", null);

            MenuManager.closeInventory(e.getWhoClicked());

            MenuManager.performActionAfterTyping(e.getWhoClicked().getUniqueId(), message -> {
                clans.databaseConnection.editStorageColor(clanName, this.index, DyeColor.valueOf(message.toUpperCase()));
                clans.messageInChat(e.getWhoClicked(), "commands.storage.recolored", Map.ofEntries(Map.entry("%new_color%", message)));
                MenuManager.open(e.getWhoClicked(), new StorageContentsMenu(clans, this.index)); // re-open this menu
            });
        }
    }
}
