package com.mrbysco.echoes.datagen.server;

import com.mrbysco.echoes.EchoesMod;
import com.mrbysco.echoes.registry.EchoTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class EchoBlockTags extends BlockTagsProvider {
	public EchoBlockTags(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
		super(packOutput, lookupProvider, EchoesMod.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(EchoTags.ECHO_SPAWNABLE_ON).add(Blocks.SCULK);
	}
}
