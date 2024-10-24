package com.mrbysco.echoes.datagen.server;

import com.mrbysco.echoes.EchoesMod;
import com.mrbysco.echoes.registry.EchoRegistry;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.AndHolderSet;

public class EchoBiomeModifiers {
	public static final ResourceKey<BiomeModifier> ADD_ECHO_CREEPER = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
			EchoesMod.modLoc("add_echo_creeper"));

	public static void bootstrap(BootstrapContext<BiomeModifier> context) {
		var biomeLookup = context.lookup(Registries.BIOME);
		HolderSet.Named<Biome> isOverworld = biomeLookup.getOrThrow(BiomeTags.IS_OVERWORLD);
		HolderSet.Named<Biome> noDefaultMonsters = biomeLookup.getOrThrow(Tags.Biomes.NO_DEFAULT_MONSTERS);

		final BiomeModifier addSpawn = BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
				new AndHolderSet<>(isOverworld, noDefaultMonsters),
				new MobSpawnSettings.SpawnerData(EchoRegistry.ECHO_CREEPER.get(), 10, 1, 1));
		context.register(ADD_ECHO_CREEPER, addSpawn);
	}
}
