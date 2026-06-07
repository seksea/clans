package me.sekc.clans;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.clip.placeholderapi.PlaceholderAPI;
import me.sekc.clans.commands.CommandManager;
import me.sekc.clans.gui.menus.FurnaceMenu;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Clans extends JavaPlugin {
    public DatabaseConnection databaseConnection;
    public YamlConfiguration messagesYml;

    String chatMessageFormat;
	boolean placeholderAPIInstalled = true;
	ClansPlaceholderExpansion placeholderExpansion;

    @Override
    public void onEnable() {
        log("\n" +
                "   .oooooo.   oooo                                 \n" +
                "  d8P'  `Y8b  `888                                 \n" +
                " 888           888   .oooo.   ooo. .oo.    .oooo.o \n" +
                " 888           888  `P  )88b  `888P\"Y88b  d88(  \"8 \n" +
                " 888           888   .oP\"888   888   888  `\"Y88b.  \n" +
                " `88b    ooo   888  d8(  888   888   888  o.  )88b \n" +
                "  `Y8bood8P'  o888o `Y888\"\"8o o888o o888o 8\"\"888P' \n" +
                "                      The ultimate clan plugin\n" +
                "==================================================="
        );

		reloadConfigFiles();

		// Initialise bStats
		if (getConfig().getBoolean("metrics")) {
			// You can find the plugin id of your plugins on
			// the page https://bstats.org/what-is-my-plugin-id
			new Metrics(this, 31862);
		}

        try {
            databaseConnection = new DatabaseConnection(getConfig().getString("database.filepath"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
			warn("Could not find PlaceholderAPI, it will not be used.");
			placeholderAPIInstalled = false;
		} else {
			log("Creating PlaceholderAPI expansion...");
			placeholderExpansion = new ClansPlaceholderExpansion(this);
			placeholderExpansion.register();
		}

        CommandManager.registerCommands(this);

        getServer().getPluginManager().registerEvents(new EventListener(this), this);

		// Furnace burning task
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
			List<String> clanList = databaseConnection.getClansWithItemsInFurnace();
			for (String clanName : clanList) {
				if (FurnaceMenu.furnaceMenuOpenedForClan.contains(clanName)) {
					continue; // Don't burn items when the furnace menu is open
				}
				List<ItemStack> itemsInFurnace = databaseConnection.getFurnaceFromClan(clanName);
				// take the first item from the furnace and burn it for xp

				int index = 0;
				for (ItemStack item : itemsInFurnace) {
					if (item.isEmpty()) {
						index++;
						continue;
					}

					DatabaseConnection.FurnaceItem fItem = databaseConnection.getSimilarItemFromFurnaceItemList(item);

					int clanXPToAdd = fItem.xp;
					int newClanXP = databaseConnection.getClanExperience(clanName) + clanXPToAdd;

					databaseConnection.setClanExperience(this, clanName, newClanXP);

					List<Player> onlinePlayersInClan = new ArrayList<>();
					for (DatabaseConnection.ClanPlayerData playerData : databaseConnection.getPlayersInClan(clanName)) {
						Player player = playerData.offlinePlayer.getPlayer();
						if (player != null) onlinePlayersInClan.add(player);
					}

					Audience clanAudience = Audience.audience(onlinePlayersInClan);
					clanAudience.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MUSIC, 1f, 1f));

					Component bossbarTitle = MiniMessage.miniMessage().deserialize(
						getMessageWithPlaceholders(null, "furnace.bossbar-title", Map.ofEntries(
							Map.entry("%clan_name%", clanName),
							Map.entry("%clan_level%", String.valueOf(databaseConnection.calculateLevel(this, newClanXP))),
							Map.entry("%clan_xp%", String.valueOf(newClanXP))
						))
					);

					BossBar bossBar = BossBar.bossBar(bossbarTitle, (float)databaseConnection.calculateLevelRatio(this, newClanXP), BossBar.Color.RED, BossBar.Overlay.NOTCHED_20);
					clanAudience.showBossBar(bossBar);

					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
						clanAudience.hideBossBar(bossBar); // Hide it (TODO: make this boss bar stick around?)
					}, getConfig().getInt("furnace.delayTicks"));

					// remove 1
					item.setAmount(item.getAmount()-1);

					databaseConnection.setItemInClanFurnace(clanName, item, index);

					break; // only burn from 1 stack
				}
			}
		}, 200, getConfig().getInt("furnace.delayTicks"));
    }

    @Override
    public void onDisable() {
        log("The clans plugin has been disabled.");
    }

	public void reloadConfigFiles() {
		boolean replaceConfigs = false; // turn this on for easy debug of default configs
		// save yml to disk if dont exist
		saveResource("config.yml", replaceConfigs);
		reloadConfig();

		chatMessageFormat = getConfig().getString("clan-message-format");
		loadMessagesYml(replaceConfigs);
	}

    public void loadMessagesYml(boolean replaceConfigs) {
        saveResource("messages.yml", /* replace */ replaceConfigs);

        messagesYml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));

        final InputStream defConfigStream = getResource("messages.yml");
        if (defConfigStream == null) {
            return;
        }

        messagesYml.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
    }

    public String getMessageWithPlaceholders(UUID playerUUID, String messageYmlPath, Map<String, String> customPlaceholders) {
        OfflinePlayer offlinePlayer = null;
        if (playerUUID != null)
            offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);

        String original = messagesYml.getString(messageYmlPath);

        if (original == null) {
            warn("MISSING " + messageYmlPath + " FROM messages.yml");
            return "MISSING " + messageYmlPath + " FROM messages.yml";
        }

        if (customPlaceholders != null) {
            for (Map.Entry<String, String> entry : customPlaceholders.entrySet()) {
                original = original.replace(entry.getKey(), entry.getValue());
            }
        }


		if (!placeholderAPIInstalled) {
			return original.stripTrailing(); // dont use PlaceholderAPI if not installed
		}
        return PlaceholderAPI.setPlaceholders(offlinePlayer, original.stripTrailing()); // strip trailing as multiline yaml strings always end with unnecessary newline
    }

    public void messageInChat(Entity playerEntity, String messageYmlPath, Map<String, String> customPlaceholders) {
        if (playerEntity instanceof Player player) {
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            chatMessageFormat + "<reset>" + getMessageWithPlaceholders(player.getUniqueId(), messageYmlPath, customPlaceholders)
                    )
            );
        } else {
            Clans.warn("Tried to send message to non-player.");
        }
    }

    public void commandResponseInChat(CommandSourceStack source, String messageYmlPath, Map<String, String> customPlaceholders) {
        messageInChat(source.getExecutor(), messageYmlPath, customPlaceholders);
    }

    static public void log(String msg) {
        Bukkit.getLogger().info("[clans] " + msg);
    }

    static public void warn(String msg) {
        Bukkit.getLogger().warning("[clans] " + msg);
    }

    static public void err(String msg) {
        Bukkit.getLogger().severe("[clans] " + msg);
    }
}
