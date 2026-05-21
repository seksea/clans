package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;
import me.sekc.clans.commands.clan.suggestions.PlayerSuggestionProvider;
import me.sekc.clans.gui.MenuManager;
import me.sekc.clans.gui.menus.InvitesMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class InvitesCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("invites")
            .executes(ctx -> {
                MenuManager.open(ctx.getSource().getExecutor(), new InvitesMenu(clans));

                return Command.SINGLE_SUCCESS;
            }).requires(sender -> {
                Entity executor = sender.getExecutor();
                if (executor == null) return false;
                return sender.getSender().hasPermission("clans.gui")
                        && clans.databaseConnection.getPlayerClan(executor.getUniqueId()).isEmpty(); // is not in clan
            })
        );
    }
}
