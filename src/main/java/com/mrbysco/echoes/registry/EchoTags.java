package com.mrbysco.echoes.registry;

import com.mrbysco.echoes.EchoesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class EchoTags {
	public static final TagKey<Block> ECHO_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, EchoesMod.modLoc("echo_spawnable_on"));
	public static final TagKey<Block> ECHO_CREEPER_REPLACE_ABLE = TagKey.create(Registries.BLOCK, EchoesMod.modLoc("echo_creeper_replace_able"));
}
