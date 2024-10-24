package com.mrbysco.echoes.entity.creeper.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import com.mrbysco.echoes.entity.creeper.EchoCreeper;
import com.mrbysco.echoes.entity.echo.Echo;
import com.mrbysco.echoes.entity.echo.ai.EchoAi;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;

public class CreeperAi {
	private static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(
			MemoryModuleType.NEAREST_LIVING_ENTITIES,
			MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
			MemoryModuleType.NEAREST_VISIBLE_PLAYER,
			MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
			MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
			MemoryModuleType.LOOK_TARGET,
			MemoryModuleType.WALK_TARGET,
			MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
			MemoryModuleType.PATH,
			MemoryModuleType.ATTACK_TARGET,
			MemoryModuleType.ATTACK_COOLING_DOWN,
			MemoryModuleType.NEAREST_ATTACKABLE,
			MemoryModuleType.DISTURBANCE_LOCATION,
			MemoryModuleType.RECENT_PROJECTILE,
			MemoryModuleType.TOUCH_COOLDOWN,
			MemoryModuleType.VIBRATION_COOLDOWN
	);

	public static void updateActivity(Echo echoCreeper) {
		echoCreeper.getBrain()
				.setActiveActivityToFirstValid(
						ImmutableList.of(Activity.FIGHT, Activity.INVESTIGATE, Activity.IDLE)
				);
	}

	public static Brain<? extends Echo> makeBrain(EchoCreeper echoCreeper, Dynamic<?> ops) {
		Brain.Provider<EchoCreeper> provider = Brain.provider(MEMORY_TYPES, EchoAi.SENSOR_TYPES);
		Brain<EchoCreeper> brain = provider.makeBrain(ops);
		EchoAi.initCoreActivity(brain);
		EchoAi.initFightActivity(echoCreeper, brain);
		EchoAi.initInvestigateActivity(brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}
}
