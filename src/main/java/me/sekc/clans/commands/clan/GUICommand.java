package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;
import me.sekc.clans.gui.menus.MainMenu;
import me.sekc.clans.gui.MenuManager;
import me.sekc.clans.gui.menus.NoClanMainMenu;
import me.sekc.clans.gui.menus.OwnerMainMenu;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class GUICommand extends BaseCommand {
    static public void openMainMenu(Clans clans, Entity player) {
        UUID playerUUID = player.getUniqueId();
        if (clans.databaseConnection.getPlayerClan(playerUUID).isEmpty()) {
            // not in a clan, open the `No Clan` main menu
            MenuManager.open(player, new NoClanMainMenu(clans));
        } else if (clans.databaseConnection.getClanOwnedByPlayer(playerUUID) != null){
            // owner of a clan, open the owner main menu
            MenuManager.open(player, new OwnerMainMenu(clans));
        } else {
            // member of a clan, open the normal main menu
            MenuManager.open(player, new MainMenu(clans));
        }
    }
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.executes(ctx -> {
            openMainMenu(clans, ctx.getSource().getExecutor());

            return Command.SINGLE_SUCCESS;
        }).requires(sender -> sender.getSender().hasPermission("clans.gui"));

		root.then(Commands.literal("gui")
			.executes(ctx -> {
				openMainMenu(clans, ctx.getSource().getExecutor());

				return Command.SINGLE_SUCCESS;
			}).requires(sender -> sender.getSender().hasPermission("clans.gui")));
    }
}
