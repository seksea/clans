package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.commands.BaseCommand;
import me.sekc.clans.commands.clan.suggestions.ClanSuggestionProvider;
import me.sekc.clans.gui.MenuManager;
import me.sekc.clans.gui.menus.StorageMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class AdminCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("admin")
            .then(Commands.literal("info") // admin version of the `info` command
                .then(Commands.argument("name", StringArgumentType.word())
                    .suggests(new ClanSuggestionProvider(clans))
                    .executes(ctx -> {
                        String clanName = ctx.getArgument("name", String.class);

                        if (!clans.databaseConnection.clanExists(clanName)) {
                            clans.commandResponseInChat(ctx.getSource(), "not-exist",
                                    Map.ofEntries(Map.entry("%clan_name%", clanName))
                            );
                            return Command.SINGLE_SUCCESS;
                        }

                        OfflinePlayer owner = Bukkit.getOfflinePlayer(clans.databaseConnection.getClanOwner(clanName));

                        Collection<DatabaseConnection.ClanPlayerData> playersInClan = clans.databaseConnection.getPlayersInClan(clanName);
                        String clanMembersString = "";
                        for (DatabaseConnection.ClanPlayerData player : playersInClan) {
                            clanMembersString += " - " + player.offlinePlayer.getName() + "<br>";
                        }

						int experience = clans.databaseConnection.getClanExperience(clanName);

                        clans.commandResponseInChat(ctx.getSource(), "admin.info-message",
							Map.ofEntries(
								Map.entry("%clan_name%", clanName),
								Map.entry("%clan_description%", clans.databaseConnection.getClanDescription(clanName)),
								Map.entry("%clan_experience%", Integer.toString(experience)),
								Map.entry("%clan_level%", Integer.toString(clans.databaseConnection.calculateLevel(clans, experience))),
								Map.entry("%clan_owner_name%", owner.getName()),
								Map.entry("%clan_num_members%", Integer.toString(playersInClan.size())),
								Map.entry("%clan_members_list%", clanMembersString)
							));
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .requires(sender -> sender.getSender().hasPermission("clans.admin"))
        );

		root.then(Commands.literal("admin")
			.then(Commands.literal("peekstorage")
				.then(Commands.argument("name", StringArgumentType.word())
					.suggests(new ClanSuggestionProvider(clans))
					.executes(ctx -> {
						String clanName = ctx.getArgument("name", String.class);

						if (!clans.databaseConnection.clanExists(clanName)) {
							clans.commandResponseInChat(ctx.getSource(), "not-exist",
								Map.ofEntries(Map.entry("%clan_name%", clanName))
							);
							return Command.SINGLE_SUCCESS;
						}

						MenuManager.open(ctx.getSource().getExecutor(), new StorageMenu(clans, clanName)); // open storage for clan
						return Command.SINGLE_SUCCESS;
					})
				)
			)
			.requires(sender -> sender.getSender().hasPermission("clans.admin"))
		);

		root.then(Commands.literal("admin")
			.then(Commands.literal("reload")
				.executes(ctx -> {
					 // refresh
					clans.commandResponseInChat(ctx.getSource(), "admin.reloading", null);

					MenuManager.clearMenuConfigCache();

					clans.reloadConfigFiles();

					clans.commandResponseInChat(ctx.getSource(), "admin.reloaded", null);
					return Command.SINGLE_SUCCESS;
				})
			)
			.requires(sender -> sender.getSender().hasPermission("clans.admin"))
		);


		root.then(Commands.literal("admin")
			.then(Commands.literal("clan_experience") // manage admin data about a clan
				.then(Commands.argument("clan_name", StringArgumentType.word())
					.suggests(new ClanSuggestionProvider(clans))
					.executes(ctx -> {
						String clanName = ctx.getArgument("clan_name", String.class);

						if (!clans.databaseConnection.clanExists(clanName)) {
							clans.commandResponseInChat(ctx.getSource(), "not-exist",
								Map.ofEntries(Map.entry("%clan_name%", clanName))
							);
							return Command.SINGLE_SUCCESS;
						}

						int val = clans.databaseConnection.getClanExperience(clanName);

						clans.commandResponseInChat(ctx.getSource(), "admin.get-experience",
							Map.ofEntries(
								Map.entry("%clan_name%", clanName),
								Map.entry("%val%", String.valueOf(val))
							));
						return Command.SINGLE_SUCCESS;
					})
					.then(Commands.argument("value", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
						.executes(ctx -> {
							String clanName = ctx.getArgument("clan_name", String.class);
							int value = ctx.getArgument("value", Integer.class);

							if (!clans.databaseConnection.clanExists(clanName)) {
								clans.commandResponseInChat(ctx.getSource(), "not-exist",
									Map.ofEntries(Map.entry("%clan_name%", clanName))
								);
								return Command.SINGLE_SUCCESS;
							}

							int oldVal = clans.databaseConnection.getClanExperience(clanName);

							clans.databaseConnection.setClanExperience(clans, clanName, value);

							clans.commandResponseInChat(ctx.getSource(), "admin.set-experience",
								Map.ofEntries(
									Map.entry("%clan_name%", clanName),
									Map.entry("%old_val%", String.valueOf(oldVal)),
									Map.entry("%new_val%", String.valueOf(value))
								));
							return Command.SINGLE_SUCCESS;
						})
					)
				)
			)
			.requires(sender -> sender.getSender().hasPermission("clans.admin"))
		);
    }
}
