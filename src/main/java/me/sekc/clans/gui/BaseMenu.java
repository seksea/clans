package me.sekc.clans.gui;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.sekc.clans.Clans;
import me.sekc.clans.gui.menus.MainMenu;
import me.sekc.clans.gui.menus.NoClanMainMenu;
import me.sekc.clans.gui.menus.StorageMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BaseMenu {
    static boolean replaceConfigs = true;

    public Clans clans;
    public YamlConfiguration menuConfiguration;

    protected class LayoutItem { // an item that has been parsed from a yaml file
        public Material material;
        public String id;
        public String name;
        public String lore;
        public boolean custom = false; // is it a `_`?

        LayoutItem(Material material, String id, String name, String lore) {
            this.material = material;
            this.id = id;
            this.name = name;
            this.lore = lore;
        }
        LayoutItem(boolean custom) {
            this.custom = custom;
        }

        public ItemStack getItemStack() {
            ItemStack item = ItemStack.of(material);

            ItemMeta meta = item.getItemMeta();

            if (name != null) {
                meta.customName(MiniMessage.miniMessage().deserialize(name));
            }

            if (lore != null) {
                List<Component> loreList = new ArrayList<>();
                for (String loreLine : lore.split("\\n")) {
                    loreList.add(MiniMessage.miniMessage().deserialize(loreLine));
                }
                meta.lore(loreList);
            }

            item.setItemMeta(meta);
            return item;
        }
    }

    // The layout, contains an item for every slot in the custom gui
    //  "." = null
    //  "_" = LayoutItem.custom = true
    protected List<LayoutItem> layoutArray = new ArrayList<>();

    public BaseMenu(Clans clans) {
        this.clans = clans;

        // Load the yml config
        clans.saveResource(getConfigPath(), /* replace */ replaceConfigs);

        menuConfiguration = YamlConfiguration.loadConfiguration(new File(clans.getDataFolder(), getConfigPath()));

        final InputStream defConfigStream = clans.getResource(getConfigPath());
        if (defConfigStream == null) {
            return;
        }

        menuConfiguration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
    }

    public String getConfigPath() { return ""; } // override me to have the yaml path

    public void fillContent(Player player, Inventory gui) {
        // Parse the items from yml
        String layoutString = menuConfiguration.getString("layout").strip();
        List<String> layoutChars = new ArrayList<>(Arrays.asList(layoutString.split("\\n| ")));

        for (String layoutChar : layoutChars) {
            if (layoutChar.equals(".")) {
                layoutArray.add(null); // Nothing in this spot
                continue;
            }
            if (layoutChar.equals("_")) {
                layoutArray.add(new LayoutItem(true)); // Custom item in this slot, override this func to customise these
                continue;
            }

            // Get the data for this item
            Material material = Material.valueOf(menuConfiguration.getString("items."+layoutChar+".material"));
            String name = menuConfiguration.getString("items."+layoutChar+".name");
            String id = menuConfiguration.getString("items."+layoutChar+".id");
            String lore = menuConfiguration.getString("items."+layoutChar+".lore");
            layoutArray.add(new LayoutItem(material, id, name, lore));
        }

        // Add the items to the inventory
        int idx = 0;
        for (LayoutItem layoutItem : layoutArray) {
            if (layoutItem == null || layoutItem.custom) {
                // Don't set this item here, if it is custom then override this func and iterate over layoutArray to fill the custom items yourself
                idx++;
                continue;
            }

            gui.setItem(idx, layoutItem.getItemStack());
            idx++;
        }
    }

    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        // Override me!

        // item ids that work everywhere!

        if (clickedItem.id != null) {
            if (clickedItem.id.equals("mainmenu")) {
                if (clans.databaseConnection.getPlayerClan(e.getWhoClicked().getUniqueId()).isEmpty()) {
                    // not in a clan, open the `No Clan` main menu
                    MenuManager.open(e.getWhoClicked(), new NoClanMainMenu(clans));
                } else {
                    // in a clan, open the normal main menu
                    MenuManager.open(e.getWhoClicked(), new MainMenu(clans));
                }
            }
        }
    }

    public void itemClicked(InventoryClickEvent e) {
        e.setCancelled(true); // always cancel by default when clicking in UI (can be uncancelled if required)

        if (e.getSlot() >= layoutArray.size() || e.getSlot() < 0) {
            return; // clicking outside the inventory is slot -999
        }

        LayoutItem item = layoutArray.get(e.getSlot());

        if (item != null) {
            layoutItemClicked(item, e);
        }
    }

    public String getTitle() { return menuConfiguration.getString("title"); }

    public int slotIdToCustomSlotID(int slotID) {
        // translates a slot ID to the id of the custom slot
        int numCustomSlotsBeforeSlotID = 0;
        int curSlotID = 0;
        for (LayoutItem item : layoutArray) {
            if (curSlotID >= slotID) {
                return item.custom ? numCustomSlotsBeforeSlotID : -1; // -1 if this slot is not custom
            }

            if (item.custom)
                numCustomSlotsBeforeSlotID++;
            curSlotID++;
        }

        return -1;
    }

    public int customSlotIDToSlotID(int customSlotID) {
        // translates a slot ID to the id of the custom slot
        int numSlotsBeforeCustomSlotID = 0;
        int curCustomSlotID = 0;
        for (LayoutItem item : layoutArray) {
            if (curCustomSlotID >= customSlotID) {
                return numSlotsBeforeCustomSlotID;
            }

            if (item.custom)
                curCustomSlotID++;
            numSlotsBeforeCustomSlotID++;
        }

        return -1; // -1 if not enough custom slots
    }
}
