package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.DatabaseConnection;
import me.sekc.clans.commands.BaseCommand;
import me.sekc.clans.commands.clan.suggestions.ClanSuggestionProvider;
import me.sekc.clans.commands.clan.suggestions.PlayerSuggestionProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class InviteCommand extends BaseCommand {
    static private void invitePlayer(Clans clans, Entity inviter, String playerName, String description) {
        UUID inviterUUID = inviter.getUniqueId();

        OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(playerName);
        UUID targetUUID = targetOfflinePlayer.getUniqueId(); // may block for web request

        String clanName = clans.databaseConnection.getPlayerClan(inviterUUID);

        Player targetPlayer = targetOfflinePlayer.getPlayer();
        if (targetPlayer != null) { // null if offline
            clans.messageInChat(targetPlayer, "invite.received", Map.ofEntries(
                    Map.entry("%clan_name%", clanName),
                    Map.entry("%description%", description)
            ));
        }
        clans.databaseConnection.sendClanInvite(clanName, description, targetUUID, inviterUUID);

        clans.messageInChat(inviter, "invite.sent", Map.ofEntries(
                Map.entry("%target_name%", playerName),
                Map.entry("%target_uuid%", targetUUID.toString())
        ));
    }

    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("invite")
            .then(Commands.argument("playername", StringArgumentType.word())
                .suggests(new PlayerSuggestionProvider())
                .executes(ctx -> {
                    String playerName = ctx.getArgument("playername", String.class);
                    invitePlayer(clans, ctx.getSource().getExecutor(), playerName, "");

                    return Command.SINGLE_SUCCESS;
                }).then(Commands.argument("description", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String playerName = ctx.getArgument("playername", String.class);
                        String description = ctx.getArgument("description", String.class);
                        invitePlayer(clans, ctx.getSource().getExecutor(), playerName, description);

                        return Command.SINGLE_SUCCESS;
                    }))
            ).requires(sender -> {
                Entity executor = sender.getExecutor();
                if (executor == null) return false;
                return sender.getSender().hasPermission("clans.invite")
                        && !clans.databaseConnection.getPlayerClan(executor.getUniqueId()).isEmpty(); // is in clan
            })
        );
    }
}
