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

    private void setItemInStorage(String clanName, ItemStack itemStack, int customSlotID) {
        clans.databaseConnection.setItemInClanStorage(clanName, itemStack, this.index, customSlotID);
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedLayoutItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedLayoutItem, e);

        ItemStack clickedItemStack = e.getCurrentItem();

        String clanName = clans.databaseConnection.getPlayerClan(e.getWhoClicked().getUniqueId());

        if (clickedLayoutItem.custom) {
            int customSlotId = this.slotIdToCustomSlotID(e.getSlot());
            ItemStack cursor = e.getCursor();

            if (e.isShiftClick()) {
                e.setCancelled(true); // don't allow any shift clicks
            } else if (customSlotId != -1) {
                if (clickedItemStack == null || clickedItemStack.isEmpty()) {
                    if (!cursor.isEmpty()) {
                        // An item has been dragged into the UI, add it and uncancel the event
                        ItemStack newItemStack = cursor.clone();

                        if (e.isRightClick()) {
                            newItemStack.setAmount(1); // if right click then add 1
                        }

                        setItemInStorage(clanName, newItemStack, customSlotId);
                        e.setCancelled(false);
                    } else {
                        // An empty slot has been clicked with nothing in cursor, do nothing
                    }
                } else {
                    if (!cursor.isEmpty()) {
                        // An item has been dragged onto another item on the UI, add it and uncancel the event
                        ItemStack newItemStack = cursor.clone();

                        if (newItemStack.isSimilar(clickedItemStack)) {
                            if (e.isRightClick()) {
                                int newAmount = Math.clamp(clickedItemStack.getAmount()+1, 1, newItemStack.getMaxStackSize());
                                newItemStack.setAmount(newAmount); // if right click and is same item then add 1
                                e.setCancelled(false);
                            }
                            if (e.isLeftClick()) {
                                int newAmount = Math.clamp(clickedItemStack.getAmount()+newItemStack.getAmount(), 1, newItemStack.getMaxStackSize());
                                newItemStack.setAmount(newAmount); // if left click and is same item then add all that we can
                                e.setCancelled(false);
                            }
                        }
                        e.setCancelled(false);
                        setItemInStorage(clanName, newItemStack, customSlotId); // swap items
                    } else {
                        if (e.isLeftClick()) {
                            // An item is being removed from the UI, uncancel and remove it from the db
                            setItemInStorage(clanName, ItemStack.empty(), customSlotId);
                            e.setCancelled(false);
                        } else if (e.isRightClick()) {
                            ItemStack newItemStack = clickedItemStack.clone();
                            newItemStack.setAmount(newItemStack.getAmount()/2);
                            setItemInStorage(clanName, newItemStack, customSlotId);
                            e.setCancelled(false);
                        }
                    }
                }
            }
        }

        if (clickedLayoutItem.id != null) {
            if (clickedLayoutItem.id.equals("back")) {
                MenuManager.open(e.getWhoClicked(), new StorageMenu(clans));
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
                    MenuManager.open(e.getWhoClicked(), new StorageContentsMenu(clans, this.index)); // re-open this menu
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
                    MenuManager.open(e.getWhoClicked(), new StorageContentsMenu(clans, this.index)); // re-open this menu
                });
            }
        }
    }
}
