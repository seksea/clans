package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InvitesMenu extends BaseMenu {
    public InvitesMenu(Clans clans) {
        super(clans);
    }

    @Override
    public String getConfigPath() {
        return "gui/storagemenu.yml";
    }

    @Override
    public void fillContent(Player player, Inventory gui) {
        super.fillContent(player, gui);

        List<DatabaseConnection.ClanInviteData> invites = clans.databaseConnection.getClanInvitesForPlayer(player.getUniqueId());

        int curIndex = 0;
        for (LayoutItem item : layoutArray) {
            // List the storages we have
            if (item != null && item.custom) {
                int customSlotId = this.slotIdToCustomSlotID(curIndex);

                if (customSlotId < invites.size()) {
                    DatabaseConnection.ClanInviteData invite = invites.get(customSlotId);
                    item.id = "invite " + invite.clanName;
                    item.material = Material.valueOf("ARROW");
                    item.name = invite.clanName;
                    item.lore = clans.getMessageWithPlaceholders(player.getUniqueId(), "invite.invite-lore", Map.ofEntries(
                            Map.entry("%description%", invite.description),
                            Map.entry("%inviter_name%", Bukkit.getOfflinePlayer(invite.inviterUUID).getName())
                    ));

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
            if (clickedItem.id.startsWith("invite ")) {
                String clanName = clickedItem.id.split(" ")[1];
                UUID playerUUID = e.getWhoClicked().getUniqueId();

				if (clans.databaseConnection.getPlayersInClan(clanName).size() >= clans.getConfig().getInt("clans.player-limit")) {
					clans.messageInChat(e.getWhoClicked(), "invite.at-max-players", null);
					return;
				}

                // delete all active invites to this clan (so if kicked then can't come back)
                clans.databaseConnection.deleteInvitesFromClan(playerUUID, clanName);
                clans.databaseConnection.addPlayerToClan(clanName, playerUUID);

                MenuManager.closeInventory(e.getWhoClicked());

                clans.messageInChat(e.getWhoClicked(), "invite.accepted", Map.ofEntries(
                        Map.entry("%clan_name%", clanName)
                ));

                ((Player)e.getWhoClicked()).updateCommands(); // refresh commands for all in-clan related commands
            }
        }
    }
}
