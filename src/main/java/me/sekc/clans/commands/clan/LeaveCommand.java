package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class LeaveCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("leave")
            .executes(ctx -> {
                UUID ownerUUID = ctx.getSource().getExecutor().getUniqueId();
                String clanName = clans.databaseConnection.getPlayerClan(ownerUUID);

                if (clanName.isEmpty()) {
                    clans.commandResponseInChat(ctx.getSource(), "commands.leave.not-in-clan", null);
                    return Command.SINGLE_SUCCESS;
                }

                if (clans.databaseConnection.getClanOwnedByPlayer(ownerUUID) != null) {
                    clans.commandResponseInChat(ctx.getSource(), "commands.leave.owner-of-clan", null);
                    return Command.SINGLE_SUCCESS;
                }

                clans.databaseConnection.playerLeaveClan(ownerUUID);

                clans.commandResponseInChat(ctx.getSource(), "commands.leave.left-clan",
                        Map.ofEntries(Map.entry("%clan_name%", clanName)));

                if (ctx.getSource().getExecutor() instanceof Player player) {
                    player.updateCommands(); // so any clan related commands from being in a clan disappear
                }

                return Command.SINGLE_SUCCESS;
            })
            .requires(sender -> {
                Entity executor = sender.getExecutor();
                if (executor == null) return false;
                return sender.getSender().hasPermission("clans.leave")
                        && !clans.databaseConnection.getPlayerClan(executor.getUniqueId()).isEmpty() // is in clan
                        && clans.databaseConnection.getClanOwnedByPlayer(executor.getUniqueId()) == null; // not clan owner
            }));
    }
}
