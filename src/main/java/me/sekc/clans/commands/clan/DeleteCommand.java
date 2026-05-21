package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;
import me.sekc.clans.gui.MenuManager;
import me.sekc.clans.gui.menus.DeleteMenu;
import me.sekc.clans.gui.menus.LeaveMenu;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class DeleteCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("delete")
            .executes(ctx -> {
                MenuManager.open(ctx.getSource().getExecutor(), new DeleteMenu(clans)); // open delete menu

                return Command.SINGLE_SUCCESS;
            })
            .requires(sender -> {
                Entity executor = sender.getExecutor();
                if (executor == null) return false;
                return sender.getSender().hasPermission("clans.delete")
                        && clans.databaseConnection.getClanOwnedByPlayer(executor.getUniqueId()) != null; // is a clan owner
            }));
    }
}
