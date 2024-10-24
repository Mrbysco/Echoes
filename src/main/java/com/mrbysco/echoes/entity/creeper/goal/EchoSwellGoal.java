package com.mrbysco.echoes.entity.creeper.goal;

import com.mrbysco.echoes.entity.creeper.EchoCreeper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class EchoSwellGoal extends Goal {
	private final EchoCreeper echoCreeper;
	@Nullable
	private LivingEntity target;

	public EchoSwellGoal(EchoCreeper creeper) {
		this.echoCreeper = creeper;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		LivingEntity livingentity = this.echoCreeper.getEntityAngryAt().orElse(this.echoCreeper.getTarget());
		return this.echoCreeper.getSwellDir() > 0 || livingentity != null && this.echoCreeper.distanceToSqr(livingentity) < 9.0;
	}

	@Override
	public void start() {
		this.echoCreeper.getNavigation().stop();
		this.target = this.echoCreeper.getEntityAngryAt().orElse(this.echoCreeper.getTarget());
	}

	@Override
	public void stop() {
		this.target = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (this.target == null) {
			this.echoCreeper.setSwellDir(-1);
		} else if (this.echoCreeper.distanceToSqr(this.target) > 49.0) {
			this.echoCreeper.setSwellDir(-1);
		} else if (!this.echoCreeper.getSensing().hasLineOfSight(this.target)) {
			this.echoCreeper.setSwellDir(-1);
		} else {
			this.echoCreeper.setSwellDir(1);
		}
	}
}