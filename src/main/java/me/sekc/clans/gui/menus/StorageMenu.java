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
import org.bukkit.inventory.Inventory;

import java.util.List;

public class StorageMenu extends BaseMenu {
    public StorageMenu(Clans clans) {
        super(clans);
    }

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
                    item.id = "slot_" + String.valueOf(customSlotId);
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
            if (clickedItem.id.startsWith("slot_")) {
                MenuManager.open(e.getWhoClicked(), new StorageContentsMenu(clans, Integer.valueOf(clickedItem.id.split("_")[1])));
            }
        }
    }
}
