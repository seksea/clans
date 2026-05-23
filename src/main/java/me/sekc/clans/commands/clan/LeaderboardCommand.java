package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;
import me.sekc.clans.gui.MenuManager;
import me.sekc.clans.gui.menus.FurnaceMenu;
import me.sekc.clans.gui.menus.LeaderboardMenu;
import org.bukkit.entity.Entity;

public class LeaderboardCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("leaderboard")
			.executes(ctx -> {
				String clanName = clans.databaseConnection.getPlayerClan(ctx.getSource().getExecutor().getUniqueId());
				MenuManager.open(ctx.getSource().getExecutor(), new LeaderboardMenu(clans));

				return Command.SINGLE_SUCCESS;
			}).requires(sender -> sender.getSender().hasPermission("clans.gui"))
		);

		root.then(Commands.literal("top") // also leaderboard command
			.executes(ctx -> {
				String clanName = clans.databaseConnection.getPlayerClan(ctx.getSource().getExecutor().getUniqueId());
				MenuManager.open(ctx.getSource().getExecutor(), new LeaderboardMenu(clans));

				return Command.SINGLE_SUCCESS;
			}).requires(sender -> sender.getSender().hasPermission("clans.gui"))
		);
    }
}
