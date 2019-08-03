package com.codeforanyone.mods.gohome;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("gohome")
public class GoHomeMod {
	private static final Logger LOGGER = LogManager.getLogger();

	// Wrappers because the original ones are ugly.
	public static final DimensionType overworld = DimensionType.field_223227_a_;
	public static final DimensionType nether = DimensionType.field_223228_b_;
	public static final DimensionType end = DimensionType.field_223229_c_;

	public GoHomeMod() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		// ne.mi.fm.lo.mo.Scanner/SCAN

	}

	private void setup(final FMLCommonSetupEvent event) {
		// some preinit code
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent startingEvent) {
		// register our server command
		new GoHomeServerCommand(startingEvent.getCommandDispatcher());
		GoHomeGlobalData.INSTANCE.load();
		LOGGER.info("GoHome mod is loading data");
	}

	@SubscribeEvent
	public void onServerStopping(FMLServerStoppingEvent stoppingEvent) {
		GoHomeGlobalData.INSTANCE.save();
		LOGGER.info("GoHome mod is saving data");
	}

	// You can use EventBusSubscriber to automatically subscribe events on the
	// contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
			// register a new block here
		}
	}
}
