package com.mrbysco.echoes.entity.echo;

import com.google.common.annotations.VisibleForTesting;
import com.mrbysco.echoes.EchoesMod;
import com.mrbysco.echoes.entity.creeper.ai.CreeperAi;
import com.mrbysco.echoes.entity.echo.ai.EchoAi;
import com.mrbysco.echoes.registry.EchoRegistry;
import com.mrbysco.echoes.registry.EchoTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.AngerManagement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class Echo extends Monster implements VibrationSystem {
	private static final EntityDataAccessor<Integer> CLIENT_ANGER_LEVEL = SynchedEntityData.defineId(Echo.class, EntityDataSerializers.INT);
	private final DynamicGameEventListener<Listener> dynamicGameEventListener;
	private final VibrationSystem.User vibrationUser;
	private VibrationSystem.Data vibrationData;
	AngerManagement angerManagement = new AngerManagement(this::canTargetEntity, Collections.emptyList());
	private int tendrilAnimation;
	private int tendrilAnimationO;
	private int heartAnimation;
	private int heartAnimationO;

	public Echo(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
		this.vibrationUser = new Echo.VibrationUser();
		this.vibrationData = new VibrationSystem.Data();
		this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
		this.getNavigation().setCanFloat(true);
		this.setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0.0F);
		this.setPathfindingMalus(PathType.DAMAGE_OTHER, 8.0F);
		this.setPathfindingMalus(PathType.POWDER_SNOW, 8.0F);
		this.setPathfindingMalus(PathType.LAVA, 8.0F);
		this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
		this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
	}

	@Override
	public float getWalkTargetValue(BlockPos pos, LevelReader level) {
		return 0.0F;
	}

	@Override
	protected boolean canRide(Entity vehicle) {
		return false;
	}

	@Override
	public boolean dampensVibrations() {
		return true;
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		this.level().broadcastEntityEvent(this, (byte) 4);
		this.playSound(EchoRegistry.ECHO_ATTACK_IMPACT.get(), 10.0F, this.getVoicePitch());
		return super.doHurtTarget(entity);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CLIENT_ANGER_LEVEL, 0);
	}

	public int getClientAngerLevel() {
		return this.entityData.get(CLIENT_ANGER_LEVEL);
	}

	private void syncClientAngerLevel() {
		this.entityData.set(CLIENT_ANGER_LEVEL, this.getActiveAnger());
	}

	@Override
	public void tick() {
		if (this.level() instanceof ServerLevel serverlevel) {
			VibrationSystem.Ticker.tick(serverlevel, this.vibrationData, this.vibrationUser);
		}

		super.tick();
		if (this.level().isClientSide()) {
			if (this.tickCount % this.getHeartBeatDelay() == 0) {
				this.heartAnimation = 10;
				if (!this.isSilent()) {
					this.level()
							.playLocalSound(
									this.getX(), this.getY(), this.getZ(), EchoRegistry.ECHO_HEARTBEAT.get(), this.getSoundSource(), 5.0F, this.getVoicePitch(), false
							);
				}
			}

			this.tendrilAnimationO = this.tendrilAnimation;
			if (this.tendrilAnimation > 0) {
				this.tendrilAnimation--;
			}

			this.heartAnimationO = this.heartAnimation;
			if (this.heartAnimation > 0) {
				this.heartAnimation--;
			}
		}
	}

	@Override
	protected void customServerAiStep() {
		ServerLevel serverlevel = (ServerLevel) this.level();
		this.getBrain().tick(serverlevel, this);
		super.customServerAiStep();

		if (this.tickCount % 20 == 0) {
			this.angerManagement.tick(serverlevel, this::canTargetEntity);
			this.syncClientAngerLevel();
		}

		CreeperAi.updateActivity(this);
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 61) {
			this.tendrilAnimation = 10;
		} else {
			super.handleEntityEvent(id);
		}
	}

	private int getHeartBeatDelay() {
		float f = (float) this.getClientAngerLevel() / (float) AngerLevel.ANGRY.getMinimumAnger();
		return 40 - Mth.floor(Mth.clamp(f, 0.0F, 1.0F) * 30.0F);
	}

	public float getTendrilAnimation(float partialTick) {
		return Mth.lerp(partialTick, (float) this.tendrilAnimationO, (float) this.tendrilAnimation) / 10.0F;
	}

	public float getHeartAnimation(float partialTick) {
		return Mth.lerp(partialTick, (float) this.heartAnimationO, (float) this.heartAnimation) / 10.0F;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Brain<Echo> getBrain() {
		return (Brain<Echo>) super.getBrain();
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer) {
		if (this.level() instanceof ServerLevel serverlevel) {
			listenerConsumer.accept(this.dynamicGameEventListener, serverlevel);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		RegistryOps<Tag> registryops = this.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		AngerManagement.codec(this::canTargetEntity)
				.encodeStart(registryops, this.angerManagement)
				.resultOrPartial(s -> EchoesMod.LOGGER.error("Failed to encode anger state for Echo Creeper: '{}'", s))
				.ifPresent(tag -> compound.put("anger", tag));
		VibrationSystem.Data.CODEC
				.encodeStart(registryops, this.vibrationData)
				.resultOrPartial(s -> EchoesMod.LOGGER.error("Failed to encode vibration listener for Echo Creeper: '{}'", s))
				.ifPresent(tag -> compound.put("listener", tag));
	}

	@Contract("null->false")
	public boolean canTargetEntity(@Nullable Entity entity) {
		if (entity instanceof LivingEntity livingentity
				&& this.level() == entity.level()
				&& EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)
				&& !this.isAlliedTo(entity)
				&& livingentity.getType() != EntityType.ARMOR_STAND
				&& livingentity.getType() != EntityType.WARDEN
				&& livingentity.getType() != EchoRegistry.ECHO_CREEPER.get()
				&& !livingentity.isInvulnerable()
				&& !livingentity.isDeadOrDying()
				&& this.level().getWorldBorder().isWithinBounds(livingentity.getBoundingBox())) {
			return true;
		}

		return false;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		RegistryOps<Tag> registryops = this.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		if (compound.contains("anger")) {
			AngerManagement.codec(this::canTargetEntity)
					.parse(registryops, compound.get("anger"))
					.resultOrPartial(s -> EchoesMod.LOGGER.error("Failed to parse anger state for Echo Creeper: '{}'", s))
					.ifPresent(p_219394_ -> this.angerManagement = p_219394_);
			this.syncClientAngerLevel();
		}

		if (compound.contains("listener", 10)) {
			VibrationSystem.Data.CODEC
					.parse(registryops, compound.getCompound("listener"))
					.resultOrPartial(s -> EchoesMod.LOGGER.error("Failed to parse vibration listener for Echo Creeper: '{}'", s))
					.ifPresent(data -> this.vibrationData = data);
		}
	}

	@Override
	public EntityType<?> getType() {
		return EchoRegistry.ECHO_CREEPER.get();
	}

	private void playListeningSound() {
		this.playSound(this.getAngerLevel().getListeningSound(), 10.0F, this.getVoicePitch());
	}

	public AngerLevel getAngerLevel() {
		return AngerLevel.byAnger(this.getActiveAnger());
	}

	private int getActiveAnger() {
		return this.angerManagement.getActiveAnger(this.getTarget());
	}

	public void clearAnger(Entity entity) {
		this.angerManagement.clearAnger(entity);
	}

	public void increaseAngerAt(@Nullable Entity entity) {
		this.increaseAngerAt(entity, 35, true);
	}

	@VisibleForTesting
	public void increaseAngerAt(@Nullable Entity entity, int offset, boolean playListeningSound) {
		if (!this.isNoAi() && this.canTargetEntity(entity)) {
			boolean flag = !(this.getTarget() instanceof Player);
			int i = this.angerManagement.increaseAnger(entity, offset);
			if (entity instanceof Player && flag && AngerLevel.byAnger(i).isAngry()) {
				this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
			}

			if (playListeningSound) {
				this.playListeningSound();
			}
		}
	}

	public Optional<LivingEntity> getEntityAngryAt() {
		return this.getAngerLevel().isAngry() ? this.angerManagement.getActiveEntity() : Optional.empty();
	}

	@Nullable
	@Override
	public LivingEntity getTarget() {
		return this.getTargetFromBrain();
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean flag = super.hurt(source, amount);
		if (!this.level().isClientSide && !this.isNoAi()) {
			Entity entity = source.getEntity();
			this.increaseAngerAt(entity, AngerLevel.ANGRY.getMinimumAnger() + 20, false);
			if (this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()
					&& entity instanceof LivingEntity livingentity
					&& (source.isDirect() || this.closerThan(livingentity, 5.0))) {
				this.setAttackTarget(livingentity);
			}
		}

		return flag;
	}

	public void setAttackTarget(LivingEntity attackTarget) {
		this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, attackTarget);
		this.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
	}

	@Override
	protected void doPush(Entity entity) {
		if (!this.isNoAi() && !this.getBrain().hasMemoryValue(MemoryModuleType.TOUCH_COOLDOWN)) {
			this.getBrain().setMemoryWithExpiry(MemoryModuleType.TOUCH_COOLDOWN, Unit.INSTANCE, 20L);
			this.increaseAngerAt(entity);
			EchoAi.setDisturbanceLocation(this, entity.blockPosition());
		}

		super.doPush(entity);
	}

	@VisibleForTesting
	public AngerManagement getAngerManagement() {
		return this.angerManagement;
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new GroundPathNavigation(this, level) {
			@Override
			protected PathFinder createPathFinder(int maxVisitedNodes) {
				this.nodeEvaluator = new WalkNodeEvaluator();
				this.nodeEvaluator.setCanPassDoors(true);
				return new PathFinder(this.nodeEvaluator, maxVisitedNodes) {
					@Override
					protected float distance(Node distanceToXZ, Node second) {
						return distanceToXZ.distanceToXZ(second);
					}
				};
			}
		};
	}

	@Override
	public VibrationSystem.Data getVibrationData() {
		return this.vibrationData;
	}

	@Override
	public VibrationSystem.User getVibrationUser() {
		return this.vibrationUser;
	}

	class VibrationUser implements VibrationSystem.User {
		private static final int GAME_EVENT_LISTENER_RANGE = 16;
		private final PositionSource positionSource = new EntityPositionSource(Echo.this, Echo.this.getEyeHeight());

		@Override
		public int getListenerRadius() {
			return GAME_EVENT_LISTENER_RANGE;
		}

		@Override
		public PositionSource getPositionSource() {
			return this.positionSource;
		}

		@Override
		public TagKey<GameEvent> getListenableEvents() {
			return GameEventTags.WARDEN_CAN_LISTEN;
		}

		@Override
		public boolean canTriggerAvoidVibration() {
			return true;
		}

		@Override
		public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos pos, Holder<GameEvent> gameEvent, GameEvent.Context context) {
			if (!Echo.this.isNoAi()
					&& !Echo.this.isDeadOrDying()
					&& !Echo.this.getBrain().hasMemoryValue(MemoryModuleType.VIBRATION_COOLDOWN)
					&& serverLevel.getWorldBorder().isWithinBounds(pos)) {
				if (context.sourceEntity() instanceof LivingEntity livingentity && !Echo.this.canTargetEntity(livingentity)) {
					return false;
				}

				return true;
			} else {
				return false;
			}
		}

		@Override
		public void onReceiveVibration(
				ServerLevel serverLevel, BlockPos pos, Holder<GameEvent> gameEvent, @Nullable Entity entity, @Nullable Entity projectile, float distance
		) {
			if (!Echo.this.isDeadOrDying()) {
				Echo.this.brain.setMemoryWithExpiry(MemoryModuleType.VIBRATION_COOLDOWN, Unit.INSTANCE, 40L);
				serverLevel.broadcastEntityEvent(Echo.this, (byte) 61);
				Echo.this.playSound(EchoRegistry.ECHO_TENDRIL_CLICKS.get(), 5.0F, Echo.this.getVoicePitch());
				BlockPos blockpos = pos;
				if (projectile != null) {
					if (Echo.this.closerThan(projectile, 30.0)) {
						if (Echo.this.getBrain().hasMemoryValue(MemoryModuleType.RECENT_PROJECTILE)) {
							if (Echo.this.canTargetEntity(projectile)) {
								blockpos = projectile.blockPosition();
							}

							Echo.this.increaseAngerAt(projectile);
						} else {
							Echo.this.increaseAngerAt(projectile, 10, true);
						}
					}

					Echo.this.getBrain().setMemoryWithExpiry(MemoryModuleType.RECENT_PROJECTILE, Unit.INSTANCE, 100L);
				} else {
					Echo.this.increaseAngerAt(entity);
				}

				if (!Echo.this.getAngerLevel().isAngry()) {
					Optional<LivingEntity> optional = Echo.this.angerManagement.getActiveEntity();
					if (projectile != null || optional.isEmpty() || optional.get() == entity) {
						setDisturbanceLocation(Echo.this, blockpos);
					}
				}
			}
		}

		public static void setDisturbanceLocation(Echo echo, BlockPos disturbanceLocation) {
			if (echo.level().getWorldBorder().isWithinBounds(disturbanceLocation)
					&& echo.getEntityAngryAt().isEmpty()
					&& echo.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()) {
				echo.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(disturbanceLocation), 100L);
				echo.getBrain().setMemoryWithExpiry(MemoryModuleType.DISTURBANCE_LOCATION, disturbanceLocation, 100L);
				echo.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
			}
		}
	}

	public static boolean checkMobSpawnRules(
			EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
	) {
		BlockPos blockpos = pos.below();
		return level.getBlockState(pos.below()).is(EchoTags.ECHO_SPAWNABLE_ON) && (spawnType == MobSpawnType.SPAWNER || level.getBlockState(blockpos).isValidSpawn(level, blockpos, type));
	}
}
