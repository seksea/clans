package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;

public class NewCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("new")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                        String clanName = ctx.getArgument("name", String.class);

                        clans.commandResponseInChat(ctx.getSource().getSender(), "Creating new clan <bold>" + clanName + "</bold>.");

                        clans.databaseConnection.createNewClan(clanName, ctx.getSource().getExecutor().getUniqueId());
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .requires(sender -> sender.getSender().hasPermission("clans.new")));
    }
}
