package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class DeleteCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("delete")
            .executes(ctx -> {
                UUID ownerUUID = ctx.getSource().getExecutor().getUniqueId();
                String clanName = clans.databaseConnection.getPlayerClan(ownerUUID);

                if (clanName.isEmpty()) {
                    clans.commandResponseInChat(ctx.getSource(), "commands.delete.not-in-clan", null);
                    return Command.SINGLE_SUCCESS;
                }

                if (!clans.databaseConnection.getClanOwnedByPlayer(ownerUUID).equals(clanName)) {
                    clans.commandResponseInChat(ctx.getSource(), "commands.delete.not-owner-of-clan", null);
                    return Command.SINGLE_SUCCESS;
                }

                clans.databaseConnection.deleteClan(clanName);

                clans.commandResponseInChat(ctx.getSource(), "commands.delete.deleted-clan",
                        Map.ofEntries(Map.entry("%clan_name%", clanName)));

                if (ctx.getSource().getExecutor() instanceof Player player) {
                    player.updateCommands(); // so any clan related commands from being in a clan disappear
                }

                return Command.SINGLE_SUCCESS;
            })
            .requires(sender -> sender.getSender().hasPermission("clans.delete")));
    }
}
