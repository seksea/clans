package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;
import me.sekc.clans.gui.MainMenu;
import me.sekc.clans.gui.MenuManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class GUICommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("gui")
                .executes(ctx -> {
                    UUID playerUUID = ctx.getSource().getExecutor().getUniqueId();

                    MenuManager.open(ctx.getSource(), new MainMenu(clans));

                    return Command.SINGLE_SUCCESS;
                })
                .requires(sender -> sender.getSender().hasPermission("clans.gui")));
    }
}
