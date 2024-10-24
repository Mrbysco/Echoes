package com.mrbysco.echoes.datagen;

import com.mrbysco.echoes.EchoesMod;
import com.mrbysco.echoes.datagen.client.EchoLanguageProvider;
import com.mrbysco.echoes.datagen.client.EchoSoundProvider;
import com.mrbysco.echoes.datagen.server.EchoBiomeModifiers;
import com.mrbysco.echoes.datagen.server.EchoBlockTags;
import com.mrbysco.echoes.datagen.server.EchoLootProvider;
import net.minecraft.core.Cloner;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class EchoesDatagen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		ExistingFileHelper fileHelper = event.getExistingFileHelper();

		if (event.includeClient()) {
			generator.addProvider(true, new EchoLanguageProvider(packOutput));
			generator.addProvider(true, new EchoSoundProvider(packOutput, fileHelper));
		}
		if (event.includeServer()) {
			generator.addProvider(true, new EchoLootProvider(packOutput, lookupProvider));
			generator.addProvider(true, new EchoBlockTags(packOutput, lookupProvider, fileHelper));

			generator.addProvider(true, new DatapackBuiltinEntriesProvider(
					packOutput, CompletableFuture.supplyAsync(EchoesDatagen::getProvider), Set.of(EchoesMod.MOD_ID)));
		}
	}

	private static RegistrySetBuilder.PatchedRegistries getProvider() {
		final RegistrySetBuilder registryBuilder = new RegistrySetBuilder();
		registryBuilder.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, EchoBiomeModifiers::bootstrap);
		// We need the BIOME registry to be present, so we can use a biome tag, doesn't matter that it's empty
		registryBuilder.add(Registries.BIOME, $ -> {
		});
		RegistryAccess.Frozen regAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		Cloner.Factory cloner$factory = new Cloner.Factory();
		net.neoforged.neoforge.registries.DataPackRegistriesHooks.getDataPackRegistriesWithDimensions().forEach(data -> data.runWithArguments(cloner$factory::addCodec));
		return registryBuilder.buildPatch(regAccess, VanillaRegistries.createLookup(), cloner$factory);
	}
}
