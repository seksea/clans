package me.sekc.clans.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.sekc.clans.Clans;

public class BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        Clans.warn("Ran base BaseCommand.register, something has gone seriously wrong");
    }
}
