package com.mrbysco.echoes.client;

import com.mrbysco.echoes.EchoesMod;
import com.mrbysco.echoes.client.model.EchoCreeperModel;
import com.mrbysco.echoes.client.renderer.EchoCreeperRenderer;
import com.mrbysco.echoes.registry.EchoRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientHandler {
	public static final ModelLayerLocation ECHO_CREEPER_LOCATION = new ModelLayerLocation(EchoesMod.modLoc("echo_creeper"), "main");

	public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(EchoRegistry.ECHO_CREEPER.get(), EchoCreeperRenderer::new);
	}

	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ECHO_CREEPER_LOCATION, EchoCreeperModel::createBodyLayer);
	}
}
