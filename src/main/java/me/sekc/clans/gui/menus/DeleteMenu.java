package me.sekc.clans.gui.menus;

import com.mojang.brigadier.Command;
import me.sekc.clans.Clans;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;
import java.util.UUID;

public class DeleteMenu extends BaseMenu {
    public DeleteMenu(Clans clans) {
        super(clans);
    }

    @Override
    public String getConfigPath() {
        return "gui/deletemenu.yml";
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        if (clickedItem.id.equals("yes")) { // delete the clan
            MenuManager.closeInventory(e.getWhoClicked());

            UUID ownerUUID = e.getWhoClicked().getUniqueId();
            String clanName = clans.databaseConnection.getPlayerClan(ownerUUID);

            if (clanName.isEmpty()) {
                clans.messageInChat(e.getWhoClicked(), "commands.delete.not-in-clan", null);
                return;
            }

            if (!clans.databaseConnection.getClanOwnedByPlayer(ownerUUID).equals(clanName)) {
                clans.messageInChat(e.getWhoClicked(), "commands.delete.not-owner-of-clan", null);
                return;
            }

            clans.databaseConnection.deleteClan(clanName);

            clans.messageInChat(e.getWhoClicked(), "commands.delete.deleted-clan",
                    Map.ofEntries(Map.entry("%clan_name%", clanName)));

            ((Player)e.getWhoClicked()).updateCommands(); // so any clan related commands from being in a clan disappear
        } else if (clickedItem.id.equals("no")) { // ignore
            MenuManager.closeInventory(e.getWhoClicked());
        }
    }
}
