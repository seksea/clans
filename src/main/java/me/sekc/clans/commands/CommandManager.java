package me.sekc.clans.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.clan.*;

public class CommandManager {
    static public void registerCommands(Clans clans) {
        Clans.log("Creating commands...");

        LiteralArgumentBuilder<CommandSourceStack> clanRoot = Commands.literal("clan");
		AdminCommand.register(clans, clanRoot);
        DeleteCommand.register(clans, clanRoot);
		FurnaceCommand.register(clans, clanRoot);
        GUICommand.register(clans, clanRoot);
        InfoCommand.register(clans, clanRoot);
        InviteCommand.register(clans, clanRoot);
        InvitesCommand.register(clans, clanRoot);
        LeaveCommand.register(clans, clanRoot);
        ManageCommand.register(clans, clanRoot);
        NewCommand.register(clans, clanRoot);
        StorageCommand.register(clans, clanRoot);
        LiteralCommandNode<CommandSourceStack> buildClanRoot = clanRoot.build();

        clans.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(buildClanRoot);
        });
    }
}
