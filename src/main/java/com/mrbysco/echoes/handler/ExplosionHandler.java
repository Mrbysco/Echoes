package com.mrbysco.echoes.handler;

import com.mrbysco.echoes.EchoesMod;
import com.mrbysco.echoes.entity.creeper.EchoCreeper;
import com.mrbysco.echoes.registry.EchoTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.List;

public class ExplosionHandler {
	public static void onDetonate(ExplosionEvent.Detonate event) {
		final Explosion explosion = event.getExplosion();
		final Level level = event.getLevel();
		List<BlockPos> affectedBlocks = event.getAffectedBlocks();
		if (!level.isClientSide) {
			if (explosion.getDirectSourceEntity() instanceof EchoCreeper) {
				for (BlockPos pos : affectedBlocks) {
					BlockState state = level.getBlockState(pos);
					EchoesMod.LOGGER.info("Block {} ", state);
					if (!state.isEmpty() && state.is(EchoTags.ECHO_CREEPER_REPLACE_ABLE)) {
						level.setBlockAndUpdate(pos, Blocks.SCULK.defaultBlockState());
					}
				}
				affectedBlocks.clear();
			}
		}
	}
}
