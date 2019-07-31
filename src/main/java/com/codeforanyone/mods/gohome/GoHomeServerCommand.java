package com.codeforanyone.mods.gohome;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nullable;

import com.codeforanyone.mods.gohome.NamedLocation.NamedLocations;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.chunk.TicketType;

public class GoHomeServerCommand {

	private static final String ARGUMENT_KEY_PLACENAME = "placename";
	private static final String CMD_DELETE_PLAYER_LOCATION = "delete";
	private static final String CMD_ADD_PLAYER_LOCATION = "add";
	private static final String CMD_DELETE_GLOBAL_LOCATION = "delete-global";
	private static final String CMD_ADD_GLOBAL_LOCATION = "add-global";
	private static final String CMD_RESET_GLOBAL_LOCATIONS = "reset-all-global";
	private static final String CMD_RESET_PLAYER_LOCATIONS = "reset-all";
	private static final String CMD_LIST = "list";
	private static final String CMD_HOME = "home";
	private static final String RESERVED_WORDS_ERROR_MESSAGE = "Error: A location name cannot be called " + CMD_HOME + " or " + CMD_LIST + ", as home is reserved for the world spawn location and list shows you the saved locations.";
	public static final int OPERATOR_PERMISSION = 2;
	public static final boolean ALLOW_LOGGING_TRUE = true;

	static class RunResult {
		static final boolean SUCCESS = true;
		static final boolean FAILURE = false;

		String message;
		boolean success;

		RunResult(boolean success, String message) {
			this.success = success;
			this.message = message;
		}
	}

	public GoHomeServerCommand(CommandDispatcher<CommandSource> dispatcher) {
		GoHomeServerCommand.register(dispatcher);
	}

	// The "then" portions are each command line argument in order.
	// The different levels of then/executes handle missing arguments with sensible
	// defaults. Note that it's .then(argument).then(argument.execute) layering
	// here.
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(Commands.literal("go")
		.then(Commands.literal(CMD_LIST).executes(ctx -> {
				return executeGoCommand(ctx.getSource(), ctx, CMD_LIST, null);
		})).then(Commands.literal(CMD_HOME).executes(ctx -> {
				return executeGoCommand(ctx.getSource(), ctx, CMD_HOME, null);
		})).then(Commands.literal(CMD_RESET_PLAYER_LOCATIONS).executes(ctx -> {
				return executeGoCommand(ctx.getSource(), ctx, CMD_RESET_PLAYER_LOCATIONS, null);
		})).then(Commands.literal(CMD_RESET_GLOBAL_LOCATIONS).executes(ctx -> {
				return executeGoCommand(ctx.getSource(), ctx, CMD_RESET_GLOBAL_LOCATIONS, null);
		})).then(Commands.literal(CMD_ADD_GLOBAL_LOCATION).then(Commands.argument(ARGUMENT_KEY_PLACENAME, StringArgumentType.word()).executes(ctx -> {
				return executeGoCommand(ctx.getSource(), ctx, CMD_ADD_GLOBAL_LOCATION,
					ctx.getArgument(ARGUMENT_KEY_PLACENAME, String.class));
		}))).then(Commands.literal(CMD_DELETE_GLOBAL_LOCATION).then(Commands.argument(ARGUMENT_KEY_PLACENAME, StringArgumentType.word()).executes(ctx -> {
				return executeGoCommand(ctx.getSource(), ctx, CMD_DELETE_GLOBAL_LOCATION,
					ctx.getArgument(ARGUMENT_KEY_PLACENAME, String.class));
		}))).then(Commands.literal(CMD_DELETE_PLAYER_LOCATION).then(Commands.argument(ARGUMENT_KEY_PLACENAME, StringArgumentType.word()).executes(ctx -> {
				return executeGoCommand(ctx.getSource(), ctx, CMD_DELETE_PLAYER_LOCATION,
					ctx.getArgument(ARGUMENT_KEY_PLACENAME, String.class));
		}))).then(Commands.literal(CMD_ADD_PLAYER_LOCATION).then(Commands.argument(ARGUMENT_KEY_PLACENAME, StringArgumentType.word()).executes(ctx -> {
				return executeGoCommand(ctx.getSource(), ctx, CMD_ADD_PLAYER_LOCATION,
					ctx.getArgument(ARGUMENT_KEY_PLACENAME, String.class));
		}))).then(Commands.argument(ARGUMENT_KEY_PLACENAME, StringArgumentType.word()).executes(ctx -> {
				return executeGoCommand(ctx.getSource(), ctx, ctx.getArgument(ARGUMENT_KEY_PLACENAME, String.class), null);
		})));
		// @formatter:on
	}

	public static RunResult add(ServerPlayerEntity player, String placename) {
		if (placename == null) {
			return new RunResult(RunResult.FAILURE, "Error: A location name must be provided for add.");
		}
		if (placename.contains(NamedLocations.SERIALIZATION_DELIMITER)) {
			return new RunResult(RunResult.FAILURE,
					"Error: A location name cannot include the " + NamedLocations.SERIALIZATION_DELIMITER + " symbol.");
		}
		if (placename.equals(CMD_HOME) || placename.equals(CMD_LIST)) {
			return new RunResult(RunResult.FAILURE,
					RESERVED_WORDS_ERROR_MESSAGE);
		}
		// Get the storage and deserialize our entire collection of places, so we can
		// modify it by adding or replacing one.
		Map<String, NamedLocation> locationsMap = NamedLocations.read(player.getEntityData());
		// Determine whether we're replacing or adding.
		boolean willOverwrite = locationsMap.containsKey(placename);
		// Do the modification.
		locationsMap.put(placename, new NamedLocation(placename, player));
		// Reserialize back into storage.
		NamedLocations.write(player.getEntityData(), locationsMap);
		// And finally give a result.
		if (willOverwrite) {
			return new RunResult(RunResult.SUCCESS, "Replaced your location " + placename);
		} else {
			return new RunResult(RunResult.SUCCESS, "Added your location " + placename);
		}

	}

	public static RunResult delete(ServerPlayerEntity player, String placename) {
		if (placename == null) {
			return new RunResult(RunResult.FAILURE, "Error: A location name must be provided for rm.");
		}
		if (placename.contains(NamedLocations.SERIALIZATION_DELIMITER)) {
			return new RunResult(RunResult.FAILURE,
					"Error: A location name cannot include the " + NamedLocations.SERIALIZATION_DELIMITER + " symbol.");
		}
		if (placename.equals(CMD_HOME) || placename.equals(CMD_LIST)) {
			return new RunResult(RunResult.FAILURE,
					RESERVED_WORDS_ERROR_MESSAGE);
		}
		// Get the storage and deserialize our entire collection of places, so we can
		// modify it by adding or replacing one.
		Map<String, NamedLocation> locationsMap = NamedLocations.read(player.getEntityData());
		// Determine whether it even existed.
		boolean itExisted = locationsMap.containsKey(placename);
		// Do the modification.
		locationsMap.remove(placename);
		// Reserialize back into storage.
		NamedLocations.write(player.getEntityData(), locationsMap);
		// And finally give a result.
		if (itExisted) {
			return new RunResult(RunResult.SUCCESS, "Deleted your named location " + placename);
		} else {
			return new RunResult(RunResult.SUCCESS, "Did not find your named location " + placename);
		}

	}

	public static RunResult resetAll(ServerPlayerEntity player) {
		// Starting fresh due to need for overwriting bad data.
		Map<String, NamedLocation> locationsMap = new HashMap<String, NamedLocation>();
		NamedLocations.write(player.getEntityData(), locationsMap);
		return new RunResult(RunResult.SUCCESS,
				"Deleted all your location names and started fresh. Hopefully that solves your problem!");
	}

	public static RunResult resetAllGlobal(CommandSource commandSource) {
		if (!commandSource.hasPermissionLevel(OPERATOR_PERMISSION)) {
			return new RunResult(RunResult.FAILURE,
					"Error: You must be a server operator to change global named locations.");
		}
		// Starting fresh due to need for overwriting bad data.
		GoHomeWorldSavedData.INSTANCE.resetAllGlobal();
		return new RunResult(RunResult.SUCCESS,
				"Deleted all global location names and started fresh. Hopefully that solves your problem!");
	}

	public static RunResult addGlobal(CommandSource commandSource, ServerPlayerEntity player, String placename) {
		if (!commandSource.hasPermissionLevel(OPERATOR_PERMISSION)) {
			return new RunResult(RunResult.FAILURE,
					"Error: You must be a server operator to add global named locations.");
		}
		if (placename == null) {
			return new RunResult(RunResult.FAILURE, "Error: A location name must be provided for add-global.");
		}
		if (placename.contains(NamedLocations.SERIALIZATION_DELIMITER)) {
			return new RunResult(RunResult.FAILURE,
					"Error: A location name cannot include the " + NamedLocations.SERIALIZATION_DELIMITER + " symbol.");
		}
		if (placename.equals(CMD_HOME) || placename.equals(CMD_LIST)) {
			return new RunResult(RunResult.FAILURE,
					RESERVED_WORDS_ERROR_MESSAGE);
		}
		boolean willOverwrite = GoHomeWorldSavedData.INSTANCE.hasNamedLocation(placename);
		GoHomeWorldSavedData.INSTANCE.addGlobalLocation(new NamedLocation(placename, player));
		if (willOverwrite) {
			return new RunResult(RunResult.SUCCESS, "Replaced global location " + placename);
		} else {
			return new RunResult(RunResult.SUCCESS, "Added global location " + placename);
		}
	}

	public static RunResult deleteGlobal(CommandSource commandSource, ServerPlayerEntity player, String placename) {
		if (!commandSource.hasPermissionLevel(OPERATOR_PERMISSION)) {
			return new RunResult(RunResult.FAILURE,
					"Error: You must be a server operator to add global named locations.");
		}
		if (placename == null) {
			return new RunResult(RunResult.FAILURE, "Error: A location name must be provided for add-global.");
		}
		if (placename.contains(NamedLocations.SERIALIZATION_DELIMITER)) {
			return new RunResult(RunResult.FAILURE,
					"Error: A location name cannot include the " + NamedLocations.SERIALIZATION_DELIMITER + " symbol.");
		}
		if (placename.equals(CMD_HOME) || placename.equals(CMD_LIST)) {
			return new RunResult(RunResult.FAILURE,
					RESERVED_WORDS_ERROR_MESSAGE);
		}

		if (GoHomeWorldSavedData.INSTANCE.hasNamedLocation(placename)) {
			GoHomeWorldSavedData.INSTANCE.removeGlobalLocation(placename);
			return new RunResult(RunResult.SUCCESS, "Removed " + placename + " from the global locations.");
		} else {
			return new RunResult(RunResult.FAILURE, "No global lcoation " + placename + " exists.  Did you typo?");
		}
	}

	public static RunResult list(ServerPlayerEntity player) {
		Map<String, NamedLocation> playerLocations = NamedLocations.read(player.getEntityData());

		SortedSet<String> globalLocationNames = GoHomeWorldSavedData.INSTANCE.listGlobalLocations();
		globalLocationNames.add(CMD_HOME);

		SortedSet<String> playerLocationNames = new TreeSet<String>();
		playerLocationNames.addAll(playerLocations.keySet());

		StringBuffer sb = new StringBuffer();
		sb.append("Global: " + String.join(", ", globalLocationNames) + "\n");

		if (playerLocationNames.isEmpty()) {
			sb.append("Yours: None.  Use '/go add placename'.\n");
		} else {
			sb.append("Yours: " + String.join(", ", playerLocationNames) + "\n");
		}

		return new RunResult(RunResult.SUCCESS, sb.toString());
	}

	public static int executeGoCommand(CommandSource commandSource, CommandContext<CommandSource> ctx, String sub,
			String placename) {

		ServerPlayerEntity player = null;
		try {
			player = commandSource.asPlayer();
		} catch (CommandSyntaxException e) {
			commandSource.sendFeedback(
					new StringTextComponent(
							"Error with reading command source as player in go command: " + e.toString()),
					ALLOW_LOGGING_TRUE);
			e.printStackTrace();
			return 0;
		}
		try {

			if (CMD_LIST.equalsIgnoreCase(sub)) {
				RunResult exitcodeGlobal = list(player);
				String globalNames = exitcodeGlobal.message;
				commandSource.sendFeedback(new StringTextComponent(globalNames), ALLOW_LOGGING_TRUE);
				return exitcodeGlobal.success ? 1 : 0;
			}
			if (CMD_RESET_PLAYER_LOCATIONS.equalsIgnoreCase(sub)) {
				RunResult exitcode = resetAll(player);
				commandSource.sendFeedback(new StringTextComponent(exitcode.message), ALLOW_LOGGING_TRUE);
				return exitcode.success ? 1 : 0;
			}
			if (CMD_RESET_GLOBAL_LOCATIONS.equalsIgnoreCase(sub)) {
				RunResult exitcode = resetAllGlobal(commandSource);
				commandSource.sendFeedback(new StringTextComponent(exitcode.message), ALLOW_LOGGING_TRUE);
				return exitcode.success ? 1 : 0;
			}
			if (CMD_ADD_GLOBAL_LOCATION.equalsIgnoreCase(sub)) {
				RunResult exitcode = addGlobal(commandSource, player, placename);
				commandSource.sendFeedback(new StringTextComponent(exitcode.message), ALLOW_LOGGING_TRUE);
				return exitcode.success ? 1 : 0;
			}
			if (CMD_DELETE_GLOBAL_LOCATION.equalsIgnoreCase(sub)) {
				RunResult exitcode = deleteGlobal(commandSource, player, placename);
				commandSource.sendFeedback(new StringTextComponent(exitcode.message), ALLOW_LOGGING_TRUE);
				return exitcode.success ? 1 : 0;
			}
			if (CMD_ADD_PLAYER_LOCATION.equalsIgnoreCase(sub)) {
				RunResult exitcode = add(player, placename);
				commandSource.sendFeedback(new StringTextComponent(exitcode.message), ALLOW_LOGGING_TRUE);
				return exitcode.success ? 1 : 0;
			}
			if (CMD_DELETE_PLAYER_LOCATION.equalsIgnoreCase(sub)) {
				RunResult exitcode = delete(player, placename);
				commandSource.sendFeedback(new StringTextComponent(exitcode.message), ALLOW_LOGGING_TRUE);
				return exitcode.success ? 1 : 0;
			}
			if (CMD_HOME.equalsIgnoreCase(sub)) {
				// TODO: Safety check first.
				RunResult exitcode = teleportHome(commandSource);
				commandSource.sendFeedback(new StringTextComponent(exitcode.message), ALLOW_LOGGING_TRUE);
				return exitcode.success ? 1 : 0;
			}
			// Check the player's named locations first, falling back on global.
			Map<String, NamedLocation> playerLocations = NamedLocations.read(player.getEntityData());
			NamedLocation destination = playerLocations.getOrDefault(sub, GoHomeWorldSavedData.INSTANCE.getNamedLocation(sub));
			if (destination != null) {
				// TODO: Safety check
				RunResult exitcode = teleportLocation(commandSource, playerLocations.get(sub));
				commandSource.sendFeedback(new StringTextComponent(exitcode.message), ALLOW_LOGGING_TRUE);
				return exitcode.success ? 1 : 0;
			} else {
				RunResult exitcode = new RunResult(RunResult.FAILURE, "Could not find location named " + sub);
				commandSource.sendFeedback(new StringTextComponent(exitcode.message), ALLOW_LOGGING_TRUE);
				return exitcode.success ? 1 : 0;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			commandSource.sendFeedback(new StringTextComponent("Error with go command: " + t.toString()), ALLOW_LOGGING_TRUE);
			return 0;
		}
	}

	private static RunResult teleportHome(CommandSource commandSource) {
		//commandSource.getEntity().changeDimension(GoHomeMod.overworld);
		GoHomeServerCommand.teleport(commandSource, commandSource.getEntity(),
				(ServerWorld) commandSource.getWorld(),
				commandSource.getWorld().getWorldInfo().getSpawnX() + 0.5,
				commandSource.getWorld().getWorldInfo().getSpawnY(),
				commandSource.getWorld().getWorldInfo().getSpawnZ() + 0.5,
				EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class), commandSource.getEntity().getYaw(0),
				commandSource.getEntity().getPitch(0), new Facing(commandSource.getEntity().getLookVec()));
		return new RunResult(RunResult.SUCCESS, "Home sweet home!");
	}

	private static RunResult teleportLocation(CommandSource commandSource, NamedLocation where) {
		//commandSource.getEntity().changeDimension(where.getDimensionType());
		GoHomeServerCommand.teleport(commandSource, commandSource.getEntity(),
				(ServerWorld) commandSource.getWorld(),
				where.getXpos(),
				where.getYpos(),
				where.getZpos(),
				EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class), commandSource.getEntity().getYaw(0),
				commandSource.getEntity().getPitch(0), new Facing(commandSource.getEntity().getLookVec()));
		return new RunResult(RunResult.SUCCESS, "Arrived at " + where.getName() + "!");
	}

	// I'd rather not copy-paste the contents of TeleportCommand, but its static
	// method is private, meaning I can't call it, so I don't have a choice. Ugh.
	private static void teleport(CommandSource source, Entity entityIn, ServerWorld worldIn, double x, double y,
			double z, Set<SPlayerPositionLookPacket.Flags> relativeList, float yaw, float pitch,
			@Nullable Facing facing) {
		if (entityIn instanceof ServerPlayerEntity) {
			ChunkPos chunkpos = new ChunkPos(new BlockPos(x, y, z));
			worldIn.getChunkProvider().func_217228_a(TicketType.POST_TELEPORT, chunkpos, 1, entityIn.getEntityId());
			entityIn.stopRiding();
			if (((ServerPlayerEntity) entityIn).isSleeping()) {
				((ServerPlayerEntity) entityIn).wakeUpPlayer(true, true, false);
			}

			if (worldIn == entityIn.world) {
				((ServerPlayerEntity) entityIn).connection.setPlayerLocation(x, y, z, yaw, pitch, relativeList);
			} else {
				((ServerPlayerEntity) entityIn).teleport(worldIn, x, y, z, yaw, pitch);
			}

			entityIn.setRotationYawHead(yaw);
		} else {
			float f1 = MathHelper.wrapDegrees(yaw);
			float f = MathHelper.wrapDegrees(pitch);
			f = MathHelper.clamp(f, -90.0F, 90.0F);
			if (worldIn == entityIn.world) {
				entityIn.setLocationAndAngles(x, y, z, f1, f);
				entityIn.setRotationYawHead(f1);
			} else {
				entityIn.detach();
				entityIn.dimension = worldIn.dimension.getType();
				Entity entity = entityIn;
				entityIn = entityIn.getType().create(worldIn);
				if (entityIn == null) {
					return;
				}

				entityIn.copyDataFromOld(entity);
				entityIn.setLocationAndAngles(x, y, z, f1, f);
				entityIn.setRotationYawHead(f1);
				worldIn.func_217460_e(entityIn);
			}
		}

		if (facing != null) {
			facing.updateLook(source, entityIn);
		}

		if (!(entityIn instanceof LivingEntity) || !((LivingEntity) entityIn).isElytraFlying()) {
			entityIn.setMotion(entityIn.getMotion().mul(1.0D, 0.0D, 1.0D));
			entityIn.onGround = true;
		}

	}

	// I'd rather not copy-paste the contents of TeleportCommand, but its static
	// method is private, meaning I can't call it, so I don't have a choice. Ugh.
	static class Facing {
		private final Vec3d position;
		private final Entity entity;
		private final EntityAnchorArgument.Type anchor;

		public Facing(Entity entityIn, EntityAnchorArgument.Type anchorIn) {
			this.entity = entityIn;
			this.anchor = anchorIn;
			this.position = anchorIn.apply(entityIn);
		}

		public Facing(Vec3d positionIn) {
			this.entity = null;
			this.position = positionIn;
			this.anchor = null;
		}

		public void updateLook(CommandSource source, Entity entityIn) {
			if (this.entity != null) {
				if (entityIn instanceof ServerPlayerEntity) {
					((ServerPlayerEntity) entityIn).lookAt(source.getEntityAnchorType(), this.entity, this.anchor);
				} else {
					entityIn.lookAt(source.getEntityAnchorType(), this.position);
				}
			} else {
				entityIn.lookAt(source.getEntityAnchorType(), this.position);
			}

		}
	}

}