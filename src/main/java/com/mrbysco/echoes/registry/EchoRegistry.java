package com.mrbysco.echoes.registry;

import com.mrbysco.echoes.EchoesMod;
import com.mrbysco.echoes.entity.creeper.EchoCreeper;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EchoRegistry {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EchoesMod.MOD_ID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, EchoesMod.MOD_ID);
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, EchoesMod.MOD_ID);

	public static final DeferredHolder<SoundEvent, SoundEvent> ECHO_ATTACK_IMPACT = SOUND_EVENTS.register("echo_attack_impact", () -> SoundEvent.createVariableRangeEvent(EchoesMod.modLoc("echo_attack_impact")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ECHO_HEARTBEAT = SOUND_EVENTS.register("echo_heartbeat", () -> SoundEvent.createVariableRangeEvent(EchoesMod.modLoc("echo_heartbeat")));
	public static final DeferredHolder<SoundEvent, SoundEvent> ECHO_TENDRIL_CLICKS = SOUND_EVENTS.register("echo_tendril_clicks", () -> SoundEvent.createVariableRangeEvent(EchoesMod.modLoc("echo_tendril_clicks")));

	public static final DeferredHolder<EntityType<?>, EntityType<EchoCreeper>> ECHO_CREEPER = ENTITY_TYPES.register("echo_creeper", () ->
			EntityType.Builder.of(EchoCreeper::new, MobCategory.MONSTER)
					.sized(0.6F, 1.7F).clientTrackingRange(8).build("echo_creeper"));


	public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
		event.register(ECHO_CREEPER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
	}

	public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
		event.put(ECHO_CREEPER.get(), EchoCreeper.createAttributes().build());
	}
}
