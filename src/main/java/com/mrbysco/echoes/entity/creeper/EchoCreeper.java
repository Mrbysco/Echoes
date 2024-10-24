package com.mrbysco.echoes.entity.creeper;

import com.mojang.serialization.Dynamic;
import com.mrbysco.echoes.entity.creeper.ai.CreeperAi;
import com.mrbysco.echoes.entity.creeper.goal.EchoSwellGoal;
import com.mrbysco.echoes.entity.echo.Echo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public class EchoCreeper extends Echo implements PowerableMob {
	private static final EntityDataAccessor<Integer> DATA_SWELL_DIR = SynchedEntityData.defineId(EchoCreeper.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_IS_POWERED = SynchedEntityData.defineId(EchoCreeper.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IS_IGNITED = SynchedEntityData.defineId(EchoCreeper.class, EntityDataSerializers.BOOLEAN);
	private int oldSwell;
	private int swell;
	private int maxSwell = 30;
	private int explosionRadius = 3;
	private int droppedSkulls;

	public EchoCreeper(EntityType<? extends EchoCreeper> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(2, new EchoSwellGoal(this));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return EchoCreeper.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return CreeperAi.makeBrain(this, dynamic);
	}

	@Override
	public int getMaxFallDistance() {
		return this.getTarget() == null ? this.getComfortableFallDistance(0.0F) : this.getComfortableFallDistance(this.getHealth() - 1.0F);
	}

	@Override
	public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
		boolean flag = super.causeFallDamage(fallDistance, multiplier, source);
		this.swell += (int) (fallDistance * 1.5F);
		if (this.swell > this.maxSwell - 5) {
			this.swell = this.maxSwell - 5;
		}

		return flag;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_SWELL_DIR, -1);
		builder.define(DATA_IS_POWERED, false);
		builder.define(DATA_IS_IGNITED, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		if (this.entityData.get(DATA_IS_POWERED)) {
			compound.putBoolean("powered", true);
		}

		compound.putShort("Fuse", (short) this.maxSwell);
		compound.putByte("ExplosionRadius", (byte) this.explosionRadius);
		compound.putBoolean("ignited", this.isIgnited());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.entityData.set(DATA_IS_POWERED, compound.getBoolean("powered"));
		if (compound.contains("Fuse", 99)) {
			this.maxSwell = compound.getShort("Fuse");
		}

		if (compound.contains("ExplosionRadius", 99)) {
			this.explosionRadius = compound.getByte("ExplosionRadius");
		}

		if (compound.getBoolean("ignited")) {
			this.ignite();
		}
	}

	@Override
	public void tick() {
		if (this.isAlive()) {
			this.oldSwell = this.swell;
			if (this.isIgnited()) {
				this.setSwellDir(1);
			}

			int i = this.getSwellDir();
			if (i > 0 && this.swell == 0) {
				this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
				this.gameEvent(GameEvent.PRIME_FUSE);
			}

			this.swell += i;
			if (this.swell < 0) {
				this.swell = 0;
			}

			if (this.swell >= this.maxSwell) {
				this.swell = this.maxSwell;
				this.explodeCreeper();
			}
		}

		super.tick();
	}

	/**
	 * Sets the active target the Goal system uses for tracking
	 */
	@Override
	public void setTarget(@Nullable LivingEntity target) {
		if (!(target instanceof Goat)) {
			super.setTarget(target);
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.CREEPER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.CREEPER_DEATH;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, damageSource, recentlyHit);
		Entity entity = damageSource.getEntity();
		if (entity != this && entity instanceof EchoCreeper creeper && creeper.canDropMobsSkull()) {
			creeper.increaseDroppedSkulls();
			this.spawnAtLocation(Items.CREEPER_HEAD);
		}
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		this.level().broadcastEntityEvent(this, (byte) 4);
		return false;
	}

	@Override
	public boolean isPowered() {
		return this.entityData.get(DATA_IS_POWERED);
	}

	/**
	 * Params: (Float)Render tick. Returns the intensity of the echo creeper's flash when it is ignited.
	 */
	public float getSwelling(float partialTicks) {
		return Mth.lerp(partialTicks, (float) this.oldSwell, (float) this.swell) / (float) (this.maxSwell - 2);
	}

	public int getSwellDir() {
		return this.entityData.get(DATA_SWELL_DIR);
	}

	/**
	 * Sets the state of echo creeper, -1 to idle and 1 to be 'in fuse'
	 */
	public void setSwellDir(int state) {
		this.entityData.set(DATA_SWELL_DIR, state);
	}

	@Override
	public void thunderHit(ServerLevel level, LightningBolt lightning) {
		super.thunderHit(level, lightning);
		this.entityData.set(DATA_IS_POWERED, true);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		if (itemstack.is(ItemTags.CREEPER_IGNITERS)) {
			SoundEvent soundevent = itemstack.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE;
			this.level()
					.playSound(player, this.getX(), this.getY(), this.getZ(), soundevent, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
			if (!this.level().isClientSide) {
				this.ignite();
				if (!itemstack.isDamageableItem()) {
					itemstack.shrink(1);
				} else {
					itemstack.hurtAndBreak(1, player, getSlotForHand(hand));
				}
			}

			return InteractionResult.sidedSuccess(this.level().isClientSide);
		} else {
			return super.mobInteract(player, hand);
		}
	}

	private void explodeCreeper() {
		if (!this.level().isClientSide) {
			float f = this.isPowered() ? 4.0F : 2.0F;
			this.dead = true;
			this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float) this.explosionRadius * f, Level.ExplosionInteraction.MOB);
			this.triggerOnDeathMobEffects(Entity.RemovalReason.KILLED);
			this.discard();
		}
	}

	public boolean isIgnited() {
		return this.entityData.get(DATA_IS_IGNITED);
	}

	public void ignite() {
		this.entityData.set(DATA_IS_IGNITED, true);
	}

	public boolean canDropMobsSkull() {
		return this.isPowered() && this.droppedSkulls < 1;
	}

	public void increaseDroppedSkulls() {
		this.droppedSkulls++;
	}
}
