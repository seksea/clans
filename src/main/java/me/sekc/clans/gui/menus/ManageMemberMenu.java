package me.sekc.clans.gui.menus;

import me.sekc.clans.Clans;
import me.sekc.clans.gui.BaseMenu;
import me.sekc.clans.gui.MenuManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ManageMemberMenu extends BaseMenu {
	String clanName;
	UUID memberUUID;

    public ManageMemberMenu(Clans clans, String clanName, UUID memberUUID) {
        super(clans);
		this.clanName = clanName;
		this.memberUUID = memberUUID;
    }

    @Override
    public String getConfigPath() {
        return "gui/manageclanmenu_member.yml";
    }

    @Override
    public void fillContent(Player player, Inventory gui) {
        super.fillContent(player, gui);

        if (clanName == null) {
            throw new RuntimeException("Player tried to get member menu when not in clan");
        }

        int curIndex = 0;
        for (LayoutItem item : layoutArray) {
			if (item != null) {
				if (item.id.equals("selectedplayer")) {
					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUUID);

					item.customItemStack = ItemStack.of(Material.PLAYER_HEAD);
					SkullMeta meta = (SkullMeta)item.customItemStack.getItemMeta();
					meta.setOwningPlayer(offlinePlayer);

					// Setup name
					String memberName = offlinePlayer.getName();
					meta.customName(Component.text(memberName).color(TextColor.color(255, 255, 255)));

					// Setup lore
					String lore = clans.getMessageWithPlaceholders(player.getUniqueId(), "manage.players.player-lore", Map.ofEntries(
						Map.entry("%player_name%", memberName),
						Map.entry("%player_uuid%", offlinePlayer.getUniqueId().toString())
					));
					List<Component> loreList = new ArrayList<>();
					for (String loreLine : lore.split("\\n")) {
						loreList.add(MiniMessage.miniMessage().deserialize(loreLine));
					}
					meta.lore(loreList);
					item.customItemStack.setItemMeta(meta);

					gui.setItem(curIndex, item.getItemStack());
				}
			}

            curIndex++;
        }
    }

    @Override
    protected void layoutItemClicked(LayoutItem clickedLayoutItem, InventoryClickEvent e) {
        super.layoutItemClicked(clickedLayoutItem, e);

        if (clickedLayoutItem.id != null) {

			if (clickedLayoutItem.id.equals("back")) {
				MenuManager.open(e.getWhoClicked(), new ManageClanMenu(clans, clanName));
			} else if (clickedLayoutItem.id.equals("kick")) {
				String playerName = Bukkit.getOfflinePlayer(memberUUID).getName();

				if (clans.databaseConnection.getClanOwnedByPlayer(memberUUID) != null) {
					clans.messageInChat(e.getWhoClicked(), "manage.players.cant-kick-owner", Map.ofEntries(
						Map.entry("%player_name%", playerName)
					));

					return;
				}
				clans.databaseConnection.removePlayerFromClan(clanName, memberUUID);

                clans.messageInChat(e.getWhoClicked(), "manage.players.kicked", Map.ofEntries(
					Map.entry("%player_name%", playerName)
				));
			}
        }
    }
}
