package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ManageMembersMenu extends BaseMenu {
	String clanName;

    public ManageMembersMenu(Clans clans, String clanName) {
        super(clans);
		this.clanName = clanName;
    }

    @Override
    public String getConfigPath() {
        return "gui/manageclanmenu_members.yml";
    }

    @Override
    public void fillContent(Player player, Inventory gui) {
        super.fillContent(player, gui);

        if (clanName == null) {
            throw new RuntimeException("Player tried to get members page when not in clan");
        }

		List<DatabaseConnection.ClanPlayerData> memberList = clans.databaseConnection.getPlayersInClan(clanName);

        int curIndex = 0;
		int customIndex = 0;
        for (LayoutItem item : layoutArray) {
			if (item != null) {
				if (customIndex >= memberList.size()) break; // no more members

				if (item.custom) {
					DatabaseConnection.ClanPlayerData memberData = memberList.get(customIndex);

					item.id = "member " + memberData.offlinePlayer.getUniqueId().toString();

					// Setup player skull
					item.customItemStack = ItemStack.of(Material.PLAYER_HEAD);
					SkullMeta meta = (SkullMeta)item.customItemStack.getItemMeta();
					meta.setOwningPlayer(memberData.offlinePlayer);

					// Setup name
					String memberName = memberData.offlinePlayer.getName();
					meta.customName(Component.text(memberName).color(TextColor.color(255, 255, 255)));

					// Setup lore
					String lore = clans.getMessageWithPlaceholders(player.getUniqueId(), "manage.players.player-lore", Map.ofEntries(
						Map.entry("%player_name%", memberName),
						Map.entry("%player_uuid%", memberData.offlinePlayer.getUniqueId().toString())
					));
					List<Component> loreList = new ArrayList<>();
					for (String loreLine : lore.split("\\n")) {
						loreList.add(MiniMessage.miniMessage().deserialize(loreLine));
					}
					meta.lore(loreList);

					item.customItemStack.setItemMeta(meta);
					gui.setItem(curIndex, item.getItemStack());

					customIndex++;
				}
			}

            curIndex++;
        }
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedLayoutItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedLayoutItem, e);

        if (clickedLayoutItem.id != null) {
			if (clickedLayoutItem.id.startsWith("member ")) {
				UUID memberUUID = UUID.fromString(clickedLayoutItem.id.split(" ")[1]);
				MenuManager.open(e.getWhoClicked(), new ManageMemberMenu(clans, clanName, memberUUID));
			}
			if (clickedLayoutItem.id.equals("back")) {
				MenuManager.open(e.getWhoClicked(), new ManageClanMenu(clans, clanName));
			}
        }
    }
}
