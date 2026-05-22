package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.Map;

public class MainMenu extends BaseMenu {
    public MainMenu(Clans clans) {
        super(clans);
    }

    @Override
    public String getConfigPath() {
        return "gui/mainmenu.yml";
    }

    @Override
    public void fillContent(Player player, Inventory gui) {
        super.fillContent(player, gui);

        int curIndex = 0;
        for (LayoutItem lItem : layoutArray) {
            if (lItem != null && lItem.id.equals("claninfo")) {
                String clanName = clans.databaseConnection.getPlayerClan(player.getUniqueId());

                if (clanName == null) {
                    throw new RuntimeException("Player tried to get storage when not in clan");
                }

                OfflinePlayer owner = Bukkit.getOfflinePlayer(clans.databaseConnection.getClanOwner(clanName));

                int numPlayersInClan = clans.databaseConnection.getPlayersInClan(clanName).size();

                lItem.lore = clans.getMessageWithPlaceholders(player.getUniqueId(), "main-menu.clan-info", Map.ofEntries(
                        Map.entry("%clan_name%", clanName),
                        Map.entry("%clan_description%", clans.databaseConnection.getClanDescription(clanName)),
                        Map.entry("%clan_owner_name%", owner.getName()),
                        Map.entry("%clan_num_members%", Integer.toString(numPlayersInClan))
                ));
                gui.setItem(curIndex, lItem.getItemStack());
            }
            curIndex++;
        }
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedItem, e);

        if (clickedItem.id != null) {
            if (clickedItem.id.equals("storage")) {
                MenuManager.open(e.getWhoClicked(), new StorageMenu(clans));
            }
            if (clickedItem.id.equals("leave")) {
                MenuManager.open(e.getWhoClicked(), new LeaveMenu(clans));
            }
            if (clickedItem.id.equals("invite")) {
                clans.messageInChat(e.getWhoClicked(), "invite.awaiting-input", null);

                MenuManager.closeInventory(e.getWhoClicked());

                MenuManager.performActionAfterTyping(e.getWhoClicked().getUniqueId(), message -> {
                    if (message.equals("cancel")) {
                        clans.messageInChat(e.getWhoClicked(), "cancelled", null);
                        return;
                    }
                    try {
                        ((Player)e.getWhoClicked()).performCommand("clan invite " + message.replace(" ", "_")); // perform `/clan new` command
                    } catch (Exception err) {
                        clans.messageInChat(e.getWhoClicked(), "invite.failed", null);
                    }
                });
            }
        }
    }
}
