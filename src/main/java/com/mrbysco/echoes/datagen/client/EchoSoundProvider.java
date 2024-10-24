package com.mrbysco.echoes.datagen.client;

import com.mrbysco.echoes.EchoesMod;
import com.mrbysco.echoes.registry.EchoRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class EchoSoundProvider extends SoundDefinitionsProvider {

	public EchoSoundProvider(PackOutput packOutput, ExistingFileHelper helper) {
		super(packOutput, EchoesMod.MOD_ID, helper);
	}

	@Override
	public void registerSounds() {
		this.add(EchoRegistry.ECHO_ATTACK_IMPACT, definition()
				.subtitle(modSubtitle(EchoRegistry.ECHO_ATTACK_IMPACT.getId()))
				.with(
						sound(ResourceLocation.parse("mob/warden/attack_impact_1")),
						sound(ResourceLocation.parse("mob/warden/attack_impact_2"))
				));
		this.add(EchoRegistry.ECHO_HEARTBEAT, definition()
				.subtitle(modSubtitle(EchoRegistry.ECHO_HEARTBEAT.getId()))
				.with(
						sound(ResourceLocation.parse("mob/warden/heartbeat_1")),
						sound(ResourceLocation.parse("mob/warden/heartbeat_2")),
						sound(ResourceLocation.parse("mob/warden/heartbeat_3")),
						sound(ResourceLocation.parse("mob/warden/heartbeat_4"))
				));
		this.add(EchoRegistry.ECHO_TENDRIL_CLICKS, definition()
				.subtitle(modSubtitle(EchoRegistry.ECHO_TENDRIL_CLICKS.getId()))
				.with(
						sound(ResourceLocation.parse("mob/warden/tendril_clicks_1")),
						sound(ResourceLocation.parse("mob/warden/tendril_clicks_2")),
						sound(ResourceLocation.parse("mob/warden/tendril_clicks_3")),
						sound(ResourceLocation.parse("mob/warden/tendril_clicks_4")),
						sound(ResourceLocation.parse("mob/warden/tendril_clicks_5")),
						sound(ResourceLocation.parse("mob/warden/tendril_clicks_6"))
				));
	}

	public String modSubtitle(ResourceLocation id) {
		return EchoesMod.MOD_ID + ".subtitle." + id.getPath();
	}

	public ResourceLocation modLoc(String name) {
		return ResourceLocation.fromNamespaceAndPath(EchoesMod.MOD_ID, name);
	}
}
