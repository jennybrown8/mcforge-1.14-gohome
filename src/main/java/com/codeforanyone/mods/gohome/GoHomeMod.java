package com.codeforanyone.mods.gohome;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
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

	// Named wrappers because the original ones are ugly from obfuscation.
	public static final DimensionType overworld = DimensionType.OVERWORLD;
	public static final DimensionType nether = DimensionType.NETHER;
	public static final DimensionType end = DimensionType.THE_END;

	public GoHomeMod() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

	}

	private void setup(final FMLCommonSetupEvent event) {
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

}
