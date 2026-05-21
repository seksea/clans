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

public class LeaveCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("leave")
            .executes(ctx -> {
                MenuManager.open(ctx.getSource().getExecutor(), new LeaveMenu(clans)); // open delete menu

                return Command.SINGLE_SUCCESS;
            }).then(Commands.literal("yes_i_am_sure").executes(ctx -> {
                UUID playerUUID = ctx.getSource().getExecutor().getUniqueId();
                String clanName = clans.databaseConnection.getPlayerClan(playerUUID);

                if (clanName.isEmpty()) {
                    clans.messageInChat(ctx.getSource().getExecutor(), "leave.not-in-clan", null);
                    return Command.SINGLE_SUCCESS;
                }

                if (clans.databaseConnection.getClanOwnedByPlayer(playerUUID) != null) {
                    clans.messageInChat(ctx.getSource().getExecutor(), "leave.owner-of-clan", null);
                    return Command.SINGLE_SUCCESS;
                }

                clans.databaseConnection.removePlayerFromClan(clanName, playerUUID);

                clans.messageInChat(ctx.getSource().getExecutor(), "leave.left-clan",
                        Map.ofEntries(Map.entry("%clan_name%", clanName)));

                if (ctx.getSource().getExecutor() instanceof Player player) {
                    player.updateCommands(); // so any clan related commands from being in a clan disappear
                }

                return Command.SINGLE_SUCCESS;
            }))
            .requires(sender -> {
                Entity executor = sender.getExecutor();
                if (executor == null) return false;
                return sender.getSender().hasPermission("clans.leave")
                        && !clans.databaseConnection.getPlayerClan(executor.getUniqueId()).isEmpty() // is in clan
                        && clans.databaseConnection.getClanOwnedByPlayer(executor.getUniqueId()) == null; // not clan owner
            }));
    }
}
