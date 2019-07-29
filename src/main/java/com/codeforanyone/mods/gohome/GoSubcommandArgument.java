package com.codeforanyone.mods.gohome;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;

public class GoSubcommandArgument implements ArgumentType<GoSubcommand> {

	private static final Iterable<String> VALID_SUBCOMMANDS = Arrays.asList("home", "add",  "add-global", "rm", "rm-global");

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