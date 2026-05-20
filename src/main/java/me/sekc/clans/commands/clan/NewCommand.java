package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("new")
            .then(Commands.argument("name", StringArgumentType.word())
                .executes(ctx -> {
                    String clanName = ctx.getArgument("name", String.class);
                    UUID ownerUUID = ctx.getSource().getExecutor().getUniqueId();

                    if (!clans.databaseConnection.getPlayerClan(ownerUUID).isEmpty()) {
                        clans.commandResponseInChat(ctx.getSource(), "commands.new.already-in-clan", null);
                        return Command.SINGLE_SUCCESS;
                    }

                    clans.databaseConnection.createNewClan(clanName, ownerUUID);
                    // set the owner of the clan to be a member of the clan
                    clans.databaseConnection.setPlayerClan(ownerUUID, clanName);

                    clans.commandResponseInChat(ctx.getSource(), "commands.new.created-new-clan",
                            Map.ofEntries(
                                    Map.entry("%clan_name%", clanName)
                            ));

                    if (ctx.getSource().getExecutor() instanceof Player player) {
                        player.updateCommands(); // so `/clans manage` shows up
                    }

                    return Command.SINGLE_SUCCESS;
                })
            )
            .requires(sender -> {
                Entity executor = sender.getExecutor();
                if (executor == null) return false;
                return sender.getSender().hasPermission("clans.new")
                        && clans.databaseConnection.getPlayerClan(executor.getUniqueId()).isEmpty(); // not in a clan
            }));
    }
}
