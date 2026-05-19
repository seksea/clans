package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;

public class InfoCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("info")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                        String clanName = ctx.getArgument("name", String.class);

                        String infoString = "";

                        clans.commandResponseInChat(ctx.getSource().getSender(), "<green>=========================================</green>");
                        clans.commandResponseInChat(ctx.getSource().getSender(), "Info for clan <bold>" + clanName + "</bold>:");
                        clans.commandResponseInChat(ctx.getSource().getSender(),  "<grey>-----------------------------------------</grey>");

                        if (clans.databaseConnection.clanExists(clanName)) {
                            clans.commandResponseInChat(ctx.getSource().getSender(), "Description: <grey>"+clans.databaseConnection.getClanDescription(clanName)+"</grey>");
                        } else {
                            clans.commandResponseInChat(ctx.getSource().getSender(), "Clan <bold>" + clanName + "</bold> does not exist.");
                        }

                        clans.commandResponseInChat(ctx.getSource().getSender(), "<green>=========================================</green>");
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .requires(sender -> sender.getSender().hasPermission("clans.info")));
    }
}
