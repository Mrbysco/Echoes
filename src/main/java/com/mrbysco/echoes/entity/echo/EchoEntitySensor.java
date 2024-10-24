package com.mrbysco.echoes.entity.echo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class EchoEntitySensor extends NearestLivingEntitySensor<Echo> {
	@Override
	public Set<MemoryModuleType<?>> requires() {
		return ImmutableSet.copyOf(Iterables.concat(super.requires(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
	}

	protected void doTick(ServerLevel level, Echo entity) {
		super.doTick(level, entity);
		getClosest(entity, p_348257_ -> p_348257_.getType() == EntityType.PLAYER)
				.or(() -> getClosest(entity, p_348258_ -> p_348258_.getType() != EntityType.PLAYER))
				.ifPresentOrElse(
						p_217841_ -> entity.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, p_217841_),
						() -> entity.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE)
				);
	}

	private static Optional<LivingEntity> getClosest(Echo warden, Predicate<LivingEntity> predicate) {
		return warden.getBrain()
				.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES)
				.stream()
				.flatMap(Collection::stream)
				.filter(warden::canTargetEntity)
				.filter(predicate)
				.findFirst();
	}

	@Override
	protected int radiusXZ() {
		return 24;
	}

	@Override
	protected int radiusY() {
		return 24;
	}
}
