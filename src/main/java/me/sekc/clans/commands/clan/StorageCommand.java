package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;
import me.sekc.clans.gui.MenuManager;
import me.sekc.clans.gui.menus.InvitesMenu;
import me.sekc.clans.gui.menus.StorageMenu;
import org.bukkit.entity.Entity;

public class StorageCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("storage")
            .executes(ctx -> {
				String clanName = clans.databaseConnection.getPlayerClan(ctx.getSource().getExecutor().getUniqueId());
                MenuManager.open(ctx.getSource().getExecutor(), new StorageMenu(clans, clanName));

                return Command.SINGLE_SUCCESS;
            }).requires(sender -> {
                Entity executor = sender.getExecutor();
                if (executor == null) return false;
                return sender.getSender().hasPermission("clans.gui")
                        && !clans.databaseConnection.getPlayerClan(executor.getUniqueId()).isEmpty(); // is in clan
            })
        );
    }
}
