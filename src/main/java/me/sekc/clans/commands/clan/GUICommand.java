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

import java.util.UUID;

public class GUICommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("gui")
                .executes(ctx -> {
                    UUID playerUUID = ctx.getSource().getExecutor().getUniqueId();

                    if (clans.databaseConnection.getPlayerClan(playerUUID).isEmpty()) {
                        // not in a clan, open the `No Clan` main menu
                        MenuManager.open(ctx.getSource().getExecutor(), new NoClanMainMenu(clans));
                    } else {
                        // in a clan, open the normal main menu
                        MenuManager.open(ctx.getSource().getExecutor(), new MainMenu(clans));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .requires(sender -> sender.getSender().hasPermission("clans.gui")));
    }
}
