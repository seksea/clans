package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;

import java.util.UUID;

public class NewCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("new")
            .then(Commands.argument("name", StringArgumentType.word())
                .executes(ctx -> {
                    String clanName = ctx.getArgument("name", String.class);
                    UUID ownerUUID = ctx.getSource().getExecutor().getUniqueId();

                    if (!clans.databaseConnection.getPlayerClan(ownerUUID).isEmpty()) {
                        clans.commandResponseInChat(ctx.getSource().getSender(), "Can't create clan as you are already in another clan, use <bold>/clan leave</bold> first.");
                        return Command.SINGLE_SUCCESS;
                    }

                    clans.commandResponseInChat(ctx.getSource().getSender(), "Creating new clan <bold>" + clanName + "</bold>.");

                    clans.databaseConnection.createNewClan(clanName, ownerUUID);
                    // set the owner of the clan to be a member of the clan
                    clans.databaseConnection.setPlayerClan(ownerUUID, clanName);

                    clans.commandResponseInChat(ctx.getSource().getSender(), "Created new clan <bold>" + clanName + "</bold>!");

                    return Command.SINGLE_SUCCESS;
                })
            )
            .requires(sender -> sender.getSender().hasPermission("clans.new")));
    }
}
