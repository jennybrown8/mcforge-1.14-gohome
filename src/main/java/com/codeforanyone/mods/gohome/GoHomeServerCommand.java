package com.codeforanyone.mods.gohome;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.codeforanyone.mods.gohome.NamedLocation.NamedLocations;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.chunk.TicketType;
import net.minecraft.world.dimension.DimensionType;

public class GoHomeServerCommand {
	
	public static final int OPERATOR_PERMISSION = 2;
	public static final boolean ALLOW_LOGGING_TRUE = true;

	public GoHomeServerCommand(CommandDispatcher<CommandSource> dispatcher) {
		GoHomeServerCommand.register(dispatcher);
	}

	// The "then" portions are each command line argument in order.
	// The different levels of then/executes handle missing arguments with sensible
	// defaults. Note that it's .then(argument).then(argument.execute) layering
	// here.

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("go")
				.then(Commands.argument("subcommand", GoSubcommandArgument.subcommands()).executes(ctx -> {
					return executeGoCommand(ctx.getSource(), ctx,
							GoSubcommandArgument.getSubcommand(ctx, "subcommand"));
				})));
	}

	public static int executeGoCommand(CommandSource commandSource, CommandContext<CommandSource> ctx, GoSubcommand sub) {
		System.out.println("Got subcommand " + sub.getSubcommandOrLocation());

		/*
		GoHomeWorldSavedData data = GoHomeWorldSavedData.INSTANCE;
		System.out.println("JENNY Before setting valtest: " + data.getValtest());
		data.setValtest("testing");
		System.out.println("JENNY After setting valtest: " + data.getValtest());
		*/

		if ("home".equalsIgnoreCase(sub.getSubcommandOrLocation())) {
			// TODO: Safety check first.
			GoHomeServerCommand.teleport(commandSource, commandSource.getEntity(), (ServerWorld) commandSource.getWorld(),
					commandSource.getWorld().getWorldInfo().getSpawnX() + 0.5, commandSource.getWorld().getWorldInfo().getSpawnY(),
					commandSource.getWorld().getWorldInfo().getSpawnZ() + 0.5,
					EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class), commandSource.getEntity().getYaw(0),
					commandSource.getEntity().getPitch(0), new Facing(commandSource.getEntity().getLookVec()));
			commandSource.sendFeedback(new StringTextComponent("Home sweet home!"), ALLOW_LOGGING_TRUE);
			return 1;
		}
		if ("list".equalsIgnoreCase(sub.getSubcommandOrLocation())) {
			commandSource.sendFeedback(new StringTextComponent("home and some others listed"), ALLOW_LOGGING_TRUE);
			return 1;
		}
		if ("add-global".equalsIgnoreCase(sub.getSubcommandOrLocation())) {
			// You must be a server operator to use this one.
			if (! commandSource.hasPermissionLevel(OPERATOR_PERMISSION)) {
					commandSource.sendFeedback(new StringTextComponent("Error: You must be a server operator to add global named locations."), ALLOW_LOGGING_TRUE);
					return 0;
			}
			commandSource.sendFeedback(new StringTextComponent("You would have been allowed to add a global location."), ALLOW_LOGGING_TRUE);
			return 1;
		}
		if ("rm-global".equalsIgnoreCase(sub.getSubcommandOrLocation())) {
			// You must be a server operator to use this one.
			if (! commandSource.hasPermissionLevel(OPERATOR_PERMISSION)) {
					commandSource.sendFeedback(new StringTextComponent("Error: You must be a server operator to remove global named locations."), ALLOW_LOGGING_TRUE);
					return 0;
			}
			commandSource.sendFeedback(new StringTextComponent("You would have been allowed to remove a global location."), ALLOW_LOGGING_TRUE);
			return 1;
		}
		if ("add".equalsIgnoreCase(sub.getSubcommandOrLocation())) {
			try {
				CompoundNBT nbt = commandSource.asPlayer().getEntityData();
				NamedLocation nl1 = new NamedLocation("testing", 55, 35, 45, DimensionType.field_223227_a_);
				Set<NamedLocation> set1 = new HashSet<NamedLocation>();
				set1.add(nl1);
				{
					Map<String, String> written = NamedLocations.serialize(set1);
					nbt.putString("names", written.get("names"));
					nbt.putString("posX", written.get("posX"));
					nbt.putString("posY", written.get("posY"));
					nbt.putString("posZ", written.get("posZ"));
					nbt.putString("dims", written.get("dims"));
				}
				{
					Map<String, String> written = new HashMap<String, String>();
					written.put("names", nbt.getString("names"));
					written.put("posX", nbt.getString("posX"));
					written.put("posY", nbt.getString("posY"));
					written.put("posZ", nbt.getString("posZ"));
					written.put("dims", nbt.getString("dims"));

					Set<NamedLocation> set2 = NamedLocations.deserialize(written);
					NamedLocation nl2 = set2.iterator().next();
					System.out.println("DEBUGGING HERE1: " + nl2.getName().equals("testing"));
					System.out.println("DEBUGGING HERE2: " + (nl2.getXpos() == 55));
					System.out.println("DEBUGGING HERE3: " + (nl2.getYpos() == 35));
					System.out.println("DEBUGGING HERE4: " + (nl2.getZpos() == 45));
					System.out.println("DEBUGGING HERE5: "
							+ (nl2.getDimensionType().getId() == DimensionType.field_223227_a_.getId()));

				}

			} catch (Throwable t) {
				t.printStackTrace();
			}
			commandSource.sendFeedback(new StringTextComponent("You would have been allowed to add a personal location."), ALLOW_LOGGING_TRUE);
			return 1;
		}
		if ("rm".equalsIgnoreCase(sub.getSubcommandOrLocation())) {
			try {
				CompoundNBT nbt = commandSource.asPlayer().getEntityData();

				Map<String, String> written = new HashMap<String, String>();
				written.put("names", nbt.getString("names"));
				written.put("posX", nbt.getString("posX"));
				written.put("posY", nbt.getString("posY"));
				written.put("posZ", nbt.getString("posZ"));
				written.put("dims", nbt.getString("dims"));

				Set<NamedLocation> set2 = NamedLocations.deserialize(written);
				NamedLocation nl2 = set2.iterator().next();
				System.out.println("DEBUGGING HERE1: " + nl2.getName().equals("testing"));
				System.out.println("DEBUGGING HERE2: " + (nl2.getXpos() == 55));
				System.out.println("DEBUGGING HERE3: " + (nl2.getYpos() == 35));
				System.out.println("DEBUGGING HERE4: " + (nl2.getZpos() == 45));
				System.out.println("DEBUGGING HERE5: "
						+ (nl2.getDimensionType().getId() == DimensionType.field_223227_a_.getId()));
			} catch (Throwable t) {
				t.printStackTrace();
			}
			commandSource.sendFeedback(new StringTextComponent("You would have been allowed to remove a personal location."), ALLOW_LOGGING_TRUE);
			return 1;
		}

		return 0;
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