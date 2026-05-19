package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.clip.placeholderapi.PlaceholderAPI;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class InfoCommand extends BaseCommand {
    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("info")
            .then(Commands.argument("name", StringArgumentType.word())
                .executes(ctx -> {
                    String clanName = ctx.getArgument("name", String.class);
                    UUID playerUUID = ctx.getSource().getExecutor().getUniqueId();

                    if (clans.databaseConnection.clanExists(clanName)) {
                        clans.commandResponseInChat(ctx.getSource().getSender(), "Clan <bold>" + clanName + "</bold> does not exist.");
                        return Command.SINGLE_SUCCESS;
                    }

                    Clans.log(clans.messagesYml.getString("commands.info.info-message"));

                    String infoString = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(playerUUID),
                            clans.messagesYml.getString("commands.info.info-message")
                                    .replace("%clan_name%", clanName)
                                    .replace("%description%", clans.databaseConnection.getClanDescription(clanName)));

                    clans.commandResponseInChat(ctx.getSource().getSender(), infoString);
                    return Command.SINGLE_SUCCESS;
                })
            )
            .requires(sender -> sender.getSender().hasPermission("clans.info"))
        );
    }
}
