package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StorageMenu extends BaseMenu {
    public StorageMenu(Clans clans) {
        super(clans);
    }

    static Set<String> storageContentsMenuOpenedForClan = new HashSet<>(); // Used so that only one player can access clan storage at once to prevent dupes

    @Override
    public String getConfigPath() {
        return "gui/storagemenu.yml";
    }

    @Override
    public void fillContent(Player player, Inventory gui) {
        super.fillContent(player, gui);

        String clanName = clans.databaseConnection.getPlayerClan(player.getUniqueId());

        if (clanName == null) {
            throw new RuntimeException("Player tried to get storage when not in clan");
        }

        List<DatabaseConnection.StorageSlot> storageSlots = clans.databaseConnection.getStorageListFromClan(clanName);

        int curIndex = 0;
        for (LayoutItem item : layoutArray) {
            // List the storages we have
            if (item != null && item.custom) {
                int customSlotId = this.slotIdToCustomSlotID(curIndex);

                if (customSlotId < storageSlots.size()) {
                    DatabaseConnection.StorageSlot slot = storageSlots.get(customSlotId);
                    item.id = "slot " + String.valueOf(customSlotId);
                    item.material = Material.valueOf(slot.color.name() + "_SHULKER_BOX");
                    item.name = slot.title;

                    gui.setItem(curIndex, item.getItemStack());
                }
            }; // only use custom items

            curIndex++;
        }
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedItem, e);

        if (clickedItem.id != null) {
            if (clickedItem.id.startsWith("slot ")) {
                String clanName = clans.databaseConnection.getPlayerClan(e.getWhoClicked().getUniqueId());

                if (storageContentsMenuOpenedForClan.contains(clanName)) { // just for now, until I can test the plugin for dupes with multiple players
                    clans.messageInChat(e.getWhoClicked(), "storage.wait-for-other-player", null);
                } else {
                    MenuManager.open(e.getWhoClicked(), new StorageContentsMenu(clans, Integer.valueOf(clickedItem.id.split(" ")[1])));
                }
            }
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent e) {
        String clanName = clans.databaseConnection.getPlayerClan(e.getPlayer().getUniqueId());

        storageContentsMenuOpenedForClan.remove(clanName);
    }
}
