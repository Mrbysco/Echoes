package com.mrbysco.echoes.registry;

import com.mrbysco.echoes.EchoesMod;
import com.mrbysco.echoes.entity.echo.EchoEntitySensor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EchoAIRegistry {
	public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(Registries.SENSOR_TYPE, EchoesMod.MOD_ID);
	public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, EchoesMod.MOD_ID);
	public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(Registries.ACTIVITY, EchoesMod.MOD_ID);

	public static final Supplier<SensorType<EchoEntitySensor>> ECHO_ENTITY_SENSOR = SENSOR_TYPES.register("nearest_echo_sensor", () -> new SensorType<>(EchoEntitySensor::new));

}
