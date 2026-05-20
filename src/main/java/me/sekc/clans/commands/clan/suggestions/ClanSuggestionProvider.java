package me.sekc.clans.commands.clan.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.sekc.clans.Clans;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ClanSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    Clans clans;

    public ClanSuggestionProvider(Clans clans) {
        this.clans = clans;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        Collection<String> clanNames = clans.databaseConnection.getAllClanNames();

        for (String playerName : clanNames) {
            builder.suggest(playerName);
        }

        return builder.buildFuture();
    }
}