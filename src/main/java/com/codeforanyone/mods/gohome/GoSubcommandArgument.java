package com.codeforanyone.mods.gohome;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

public class GoSubcommandArgument implements ArgumentType<GoSubcommand> {

	// This list shows on screen during autocomplete.
	public static final List<String> VALID_SUBCOMMANDS = Arrays.asList("home", "list", "add",  "add-global", "rm", "rm-global");

	private GoSubcommandArgument() {
	}

	public static ArgumentType<GoSubcommand> subcommands() {
		return new GoSubcommandArgument();
	}

	public static GoSubcommand getSubcommand(CommandContext<CommandSource> context, String argumentName) {
		return context.getArgument(argumentName, GoSubcommand.class);
	}

	
	// This method gets called after every keystroke.
	public GoSubcommand parse(StringReader reader) throws CommandSyntaxException {
        final String key = reader.readString();
        return Optional.of(GoSubcommand.fromString(key.toString())).get();
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_listSuggestions_1_,
			SuggestionsBuilder builder) {
		return ISuggestionProvider.suggest(VALID_SUBCOMMANDS, builder);
	}

	public Collection<String> getExamples() {
		return (Collection<String>) VALID_SUBCOMMANDS;
	}


}