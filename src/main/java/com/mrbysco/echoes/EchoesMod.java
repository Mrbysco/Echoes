package com.mrbysco.echoes;

import com.mojang.logging.LogUtils;
import com.mrbysco.echoes.client.ClientHandler;
import com.mrbysco.echoes.registry.EchoAIRegistry;
import com.mrbysco.echoes.registry.EchoRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(EchoesMod.MOD_ID)
public class EchoesMod {
	public static final String MOD_ID = "echoes";
	public static final Logger LOGGER = LogUtils.getLogger();

	public EchoesMod(IEventBus eventBus, Dist dist, ModContainer container) {
		EchoRegistry.ITEMS.register(eventBus);
		EchoRegistry.ENTITY_TYPES.register(eventBus);
		EchoRegistry.SOUND_EVENTS.register(eventBus);
		EchoAIRegistry.SENSOR_TYPES.register(eventBus);
		EchoAIRegistry.MEMORY_MODULE_TYPES.register(eventBus);
		EchoAIRegistry.ACTIVITIES.register(eventBus);

		eventBus.addListener(EchoRegistry::registerSpawnPlacements);
		eventBus.addListener(EchoRegistry::registerEntityAttributes);

		if (dist.isClient()) {
			eventBus.addListener(ClientHandler::registerEntityRenders);
			eventBus.addListener(ClientHandler::registerLayerDefinitions);
		}
	}

	public static ResourceLocation modLoc(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
