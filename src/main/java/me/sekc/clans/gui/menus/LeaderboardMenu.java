package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaderboardMenu extends BaseMenu {
    public LeaderboardMenu(Clans clans) {
        super(clans);
    }

    @Override
    public String getConfigPath() {
        return "gui/leaderboardmenu.yml";
    }

    @Override
    public void fillContent(Player player, Inventory gui) {
        super.fillContent(player, gui);

        List<String> leaderboard = clans.databaseConnection.getClanLeaderboardNames(); // ordered

        int curIndex = 0;
        for (LayoutItem item : layoutArray) {
            // List the storages we have
            if (item != null && item.custom) {
                int customSlotId = this.slotIdToCustomSlotID(curIndex);

                if (customSlotId < leaderboard.size()) {
                    String clanName = leaderboard.get(customSlotId);

                    item.id = "leaderboard " + clanName;
                    item.material = Material.PAPER;
                    item.name = clanName;


					OfflinePlayer owner = Bukkit.getOfflinePlayer(clans.databaseConnection.getClanOwner(clanName));

					int numPlayersInClan = clans.databaseConnection.getPlayersInClan(clanName).size();

					int experience = clans.databaseConnection.getClanExperience(clanName);

					item.lore = clans.getMessageWithPlaceholders(player.getUniqueId(), "main-menu.clan-info", Map.ofEntries(
						Map.entry("%clan_name%", clanName),
						Map.entry("%clan_description%", clans.databaseConnection.getClanDescription(clanName)),
						Map.entry("%clan_experience%", Integer.toString(experience)),
						Map.entry("%clan_level%", Integer.toString(clans.databaseConnection.calculateLevel(clans, experience))),
						Map.entry("%clan_owner_name%", owner.getName()),
						Map.entry("%clan_num_members%", Integer.toString(numPlayersInClan))
					));

                    gui.setItem(curIndex, item.getItemStack());
                }
            };

            curIndex++;
        }
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedItem, e);
    }
}
