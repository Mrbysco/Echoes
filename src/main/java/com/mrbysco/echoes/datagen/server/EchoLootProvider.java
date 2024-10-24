package com.mrbysco.echoes.datagen.server;

import com.mrbysco.echoes.registry.EchoRegistry;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EchoLootProvider extends LootTableProvider {
	public EchoLootProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, Set.of(), List.of(
				new SubProviderEntry(EchoLoot::new, LootContextParamSets.ENTITY)
		), lookupProvider);
	}

	@Override
	protected void validate(WritableRegistry<LootTable> writableregistry, ValidationContext validationcontext, ProblemReporter.Collector problemreporter$collector) {
		super.validate(writableregistry, validationcontext, problemreporter$collector);
	}

	private static class EchoLoot extends EntityLootSubProvider {
		protected EchoLoot(HolderLookup.Provider provider) {
			super(FeatureFlags.REGISTRY.allFlags(), provider);
		}

		@Override
		public void generate() {
			this.add(
					EchoRegistry.ECHO_CREEPER.get(),
					LootTable.lootTable()
							.withPool(
									LootPool.lootPool()
											.setRolls(ConstantValue.exactly(1.0F))
											.add(
													LootItem.lootTableItem(Items.GUNPOWDER)
															.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
															.apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0.0F, 1.0F)))
											)
							)
							.withPool(
									LootPool.lootPool()
											.add(TagEntry.expandTag(ItemTags.CREEPER_DROP_MUSIC_DISCS))
											.when(
													LootItemEntityPropertyCondition.hasProperties(
															LootContext.EntityTarget.ATTACKER, EntityPredicate.Builder.entity().of(EntityTypeTags.SKELETONS)
													)
											)
							)
			);
		}

		@Override
		protected Stream<EntityType<?>> getKnownEntityTypes() {
			return EchoRegistry.ENTITY_TYPES.getEntries().stream().map(Supplier::get);
		}
	}
}
