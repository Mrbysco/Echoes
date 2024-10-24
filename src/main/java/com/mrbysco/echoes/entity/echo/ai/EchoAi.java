package com.mrbysco.echoes.entity.echo.ai;

import com.google.common.collect.ImmutableList;
import com.mrbysco.echoes.entity.echo.Echo;
import com.mrbysco.echoes.registry.EchoAIRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.warden.SetWardenLookTarget;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;

public class EchoAi {
	public static final List<SensorType<? extends Sensor<? super Echo>>> SENSOR_TYPES = List.of(
			SensorType.NEAREST_PLAYERS, EchoAIRegistry.ECHO_ENTITY_SENSOR.get());

	public static void initCoreActivity(Brain<? extends Echo> brain) {
		brain.addActivity(
				Activity.CORE, 0, ImmutableList.of(new Swim(0.8F), SetWardenLookTarget.create(), new LookAtTargetSink(45, 90), new MoveToTargetSink())
		);
	}

	public static void initFightActivity(Echo echo, Brain<? extends Echo> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
				Activity.FIGHT,
				10,
				ImmutableList.of(
						StopAttackingIfTargetInvalid.<Echo>create(
								entity -> !echo.getAngerLevel().isAngry() ||
										!echo.canTargetEntity(entity), EchoAi::onTargetInvalid, false
						),
						SetEntityLookTarget.create(p_219535_ -> isTarget(echo, p_219535_), (float) echo.getAttributeValue(Attributes.FOLLOW_RANGE)),
						SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.2F),
						MeleeAttack.create(18)
				),
				MemoryModuleType.ATTACK_TARGET
		);
	}

	public static boolean isTarget(Echo echo, LivingEntity entity) {
		return echo.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(p_219509_ -> p_219509_ == entity).isPresent();
	}

	public static void onTargetInvalid(Echo echo, LivingEntity target) {
		if (!echo.canTargetEntity(target)) {
			echo.clearAnger(target);
		}
	}

	public static void initInvestigateActivity(Brain<? extends Echo> brain) {
		brain.addActivityAndRemoveMemoryWhenStopped(
				Activity.INVESTIGATE,
				5,
				ImmutableList.of(GoToTargetLocation.create(MemoryModuleType.DISTURBANCE_LOCATION, 2, 0.7F)),
				MemoryModuleType.DISTURBANCE_LOCATION
		);
	}

	public static void setDisturbanceLocation(Echo echo, BlockPos disturbanceLocation) {
		if (echo.level().getWorldBorder().isWithinBounds(disturbanceLocation)
				&& !echo.getEntityAngryAt().isPresent()
				&& !echo.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
			echo.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(disturbanceLocation), 100L);
			echo.getBrain().setMemoryWithExpiry(MemoryModuleType.DISTURBANCE_LOCATION, disturbanceLocation, 100L);
			echo.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		}
	}
}
