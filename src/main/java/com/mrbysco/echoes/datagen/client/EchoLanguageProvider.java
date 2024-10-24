package com.mrbysco.echoes.datagen.client;

import com.mrbysco.echoes.EchoesMod;
import com.mrbysco.echoes.registry.EchoRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

public class EchoLanguageProvider extends LanguageProvider {
	public EchoLanguageProvider(PackOutput packOutput) {
		super(packOutput, EchoesMod.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		this.addEntityType(EchoRegistry.ECHO_CREEPER, "Echo Creeper");
		this.addSubtitle(EchoRegistry.ECHO_ATTACK_IMPACT, "Echo lands hit");
		this.addSubtitle(EchoRegistry.ECHO_HEARTBEAT, "Echo's heart beats");
		this.addSubtitle(EchoRegistry.ECHO_TENDRIL_CLICKS, "Echo's Tendrils Click");
	}

	/**
	 * Add a subtitle to a sound event
	 *
	 * @param sound The sound event registry object
	 * @param text  The subtitle text
	 */
	public void addSubtitle(DeferredHolder<SoundEvent, SoundEvent> sound, String text) {
		this.addSubtitle(sound.get(), text);
	}

	/**
	 * Add a subtitle to a sound event
	 *
	 * @param sound The sound event
	 * @param text  The subtitle text
	 */
	public void addSubtitle(SoundEvent sound, String text) {
		String path = EchoesMod.MOD_ID + ".subtitle." + sound.getLocation().getPath();
		this.add(path, text);
	}
}
