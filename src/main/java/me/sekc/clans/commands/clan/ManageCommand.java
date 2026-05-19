package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;

public class ManageCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("manage")
            .then(Commands.argument("clan_name", StringArgumentType.word())
                .then(Commands.literal("description")
                    .then(Commands.argument("new_description", StringArgumentType.string())
                        .executes(ctx -> {
                            String clanName = ctx.getArgument("clan_name", String.class);
                            String newDescription = ctx.getArgument("new_description", String.class);

                            clans.commandResponseInChat(ctx.getSource().getSender(), "Updating description of clan <bold>" + clanName + "</bold> to: <grey>" + newDescription + "</grey>");

                            clans.databaseConnection.setClanDescription(clanName, newDescription);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            )
            .requires(sender -> sender.getSender().hasPermission("clans.manage")));
    }
}
