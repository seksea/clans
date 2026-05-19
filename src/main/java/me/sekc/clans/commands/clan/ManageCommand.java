package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;

import java.util.Map;

public class ManageCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("manage")
            .then(Commands.argument("clan_name", StringArgumentType.word())
                .then(Commands.literal("description")
                    .then(Commands.argument("new_description", StringArgumentType.string())
                        .executes(ctx -> {
                            String clanName = ctx.getArgument("clan_name", String.class);
                            String newDescription = ctx.getArgument("new_description", String.class);

                            clans.databaseConnection.setClanDescription(clanName, newDescription);

                            clans.commandResponseInChat(ctx.getSource(), "commands.manage.update-var",
                                    Map.ofEntries(
                                            Map.entry("%var_name%", "description"),
                                            Map.entry("%clan_name%", clanName),
                                            Map.entry("%new_value%", newDescription)
                                    ));
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            )
            .requires(sender -> sender.getSender().hasPermission("clans.manage")));
    }
}
