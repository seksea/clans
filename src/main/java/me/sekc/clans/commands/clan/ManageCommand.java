package me.sekc.clans.commands.clan;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.sekc.clans.Clans;
import me.sekc.clans.commands.BaseCommand;
import org.bukkit.entity.Entity;

import java.util.Map;

public class ManageCommand extends BaseCommand {
    public interface GetSettingRunnable {
        public String get(String clanName);
    }

    public interface SetSettingRunnable {
        public void set(String clanName, String newValue);
    }

    static private void registerEditSettingSubCommand(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root, String varName, GetSettingRunnable getSetting, SetSettingRunnable setSetting) {
        String argumentName = "new_"+varName;
        root.then(Commands.literal("manage")
                .then(Commands.literal(varName).executes(ctx -> {
                            String clanName = clans.databaseConnection.getClanOwnedByPlayer(ctx.getSource().getExecutor().getUniqueId());

                            clans.commandResponseInChat(ctx.getSource(), "commands.manage.get-var",
                                    Map.ofEntries(
                                            Map.entry("%var_name%", varName),
                                            Map.entry("%clan_name%", clanName),
                                            Map.entry("%value%", getSetting.get(clanName))
                                    ));
                            return Command.SINGLE_SUCCESS;
                        }).then(Commands.argument(argumentName, StringArgumentType.string())
                                .executes(ctx -> {
                                    String clanName = clans.databaseConnection.getClanOwnedByPlayer(ctx.getSource().getExecutor().getUniqueId());
                                    String newValue = ctx.getArgument(argumentName, String.class);

                                    setSetting.set(clanName, newValue);

                                    clans.commandResponseInChat(ctx.getSource(), "commands.manage.update-var",
                                            Map.ofEntries(
                                                    Map.entry("%var_name%", varName),
                                                    Map.entry("%clan_name%", clanName),
                                                    Map.entry("%new_value%", newValue)
                                            ));
                                    return Command.SINGLE_SUCCESS;
                                }))
                )
                .requires(sender -> {
                    Entity executor = sender.getExecutor();
                    if (executor == null) return false;
                    return sender.getSender().hasPermission("clans.manage")
                            && clans.databaseConnection.getClanOwnedByPlayer(executor.getUniqueId()) != null;
                })
        );
    }

    static public void register(Clans clans, LiteralArgumentBuilder<CommandSourceStack> root) {
        registerEditSettingSubCommand(
                clans, root,
                "description",
                clanName -> clans.databaseConnection.getClanDescription(clanName),
                (clanName, newValue) -> clans.databaseConnection.setClanDescription(clanName, newValue));
    }
}
