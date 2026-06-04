package com.avaritia.common.entity.arrow;

import com.avaritia.init.registry.ModDamageTypes;
import com.avaritia.init.registry.ModEntityTypes;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Name: Avaritia-forge / TraceArrowEntity
 * Author: cnlimiter
 * CreateTime: 2023/9/23 16:34
 * Description:
 */

public class TraceArrowEntity extends Arrow {
    private static final List<String> PROJECTILE_ANTI_IMMUNE_ENTITIES = List.of("minecraft:enderman", "minecraft:wither", "minecraft:ender_dragon", "draconicevolution:guardian_wither");
    private static final EntityDataAccessor<Integer> SPECTRAL_TIME = SynchedEntityData.defineId(TraceArrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> JUMP_COUNT = SynchedEntityData.defineId(TraceArrowEntity.class, EntityDataSerializers.INT);
    private final Entity owner = this.getOwner() == null ? this : this.getOwner();
    private LivingEntity homingTarget;
    private Vec3 seekOrigin;
    private int homingTime;

    public TraceArrowEntity(EntityType<? extends Arrow> entityType, Level world) {
        super(entityType, world);
        this.homingTarget = null;
        this.seekOrigin = null;
        this.homingTime = 0;
    }

    public TraceArrowEntity(Level world, Entity pShooter, double xPos, double yPos, double zPos) {
        this(ModEntityTypes.TRACE_ARROW.get(), world);
        this.setOwner(pShooter);
        this.setPos(xPos, yPos, zPos);
    }

    public TraceArrowEntity(Level world, Entity pShooter) {
        this(world, pShooter,  pShooter.getX(), pShooter.getEyeY() - (double)0.1F, pShooter.getZ());
        if (pShooter instanceof Player) {
            this.pickup = AbstractArrow.Pickup.ALLOWED;
        }
    }

    public TraceArrowEntity(Entity pShooter) {
        this(pShooter.level(), pShooter);
    }

    public void setSpectral(int spectralTime) {
        this.entityData.set(SPECTRAL_TIME, spectralTime);
    }

    public int getSpectralTime() {
        return this.entityData.get(SPECTRAL_TIME);
    }

    public int getJumpCount() {
        return this.entityData.get(JUMP_COUNT);
    }

    public void setJumpCount(int jumpCount) {
        this.entityData.set(JUMP_COUNT, jumpCount);
    }

    @Override
    public void tick() {
        this.updateHoming();
        super.tick();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        if (pResult.getEntity() instanceof LivingEntity livingEntity)
            livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false,false));
        infinityTraceArrowDamage(pResult);
    }

    private void infinityTraceArrowDamage(@NotNull EntityHitResult result) {
        Entity entity = result.getEntity();
        if (entity instanceof Player) {
            seekNextTarget();
            return;
        }

        float speed = (float) this.getDeltaMovement().length();
        int damage = Mth.ceil(Mth.clamp((double) speed * this.getBaseTraceDamage(), 0.0D, 2.147483647E9D));
        Entity owner = this.getOwner() == null ? this : this.getOwner();
        if (this.isCritArrow()) {
            long bonus = this.getRandom().nextInt(damage / 2 + 2);
            damage = (int) Math.min(bonus + (long) damage, Integer.MAX_VALUE);
        }

        DamageSource damageSource = getArrowDamageSource(owner, entity);
        boolean isEnderman = entity.getType() == EntityType.ENDERMAN;
        int oldFireTicks = entity.getRemainingFireTicks();
        if (this.isOnFire() && !isEnderman) {
            entity.setRemainingFireTicks(5);
        }

        if (entity instanceof Player player && player.isUsingItem() && player.getUseItem().getItem() instanceof ShieldItem) {
            player.getCooldowns().addCooldown(player.getUseItem(), 100);
            this.level().broadcastEntityEvent(player, (byte) 30);
            player.stopUsingItem();
        }

        if (damageEntity(entity, damageSource, (float) damage)) {
            if (entity instanceof LivingEntity livingEntity) {
                if (!this.level().isClientSide() && this.getPierceLevel() <= 0) {
                    livingEntity.setArrowCount(livingEntity.getArrowCount() + 1);
                }

                if (this.level() instanceof ServerLevel serverLevel && owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, livingEntity, damageSource, this.getWeaponItem());
                }

                this.doPostHurtEffects(livingEntity);
                if (livingEntity != owner && livingEntity instanceof Player && owner instanceof ServerPlayer serverPlayer && !this.isSilent()) {
                    serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PLAY_ARROW_HIT_SOUND, 0.0F));
                }

                if (!this.level().isClientSide() && owner instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.KILLED_BY_ARROW.trigger(serverPlayer, List.of(entity), this.getWeaponItem());
                }
            }

            this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.getRandom().nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.setDeltaMovement(entity.getDeltaMovement().scale(0.0D));
                this.setPos(entity.position());
                this.seekNextTarget();
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
            }
        } else {
            entity.setRemainingFireTicks(oldFireTicks);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.0D));
            this.setYRot(this.getYRot() + 180.0F);
            this.setPos(entity.position());
            this.yRotO += 180.0F;
            if (!this.level().isClientSide() && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
                if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    if (this.level() instanceof ServerLevel serverLevel) {
                        this.spawnAtLocation(serverLevel, this.getPickupItem(), 0.1F);
                    }
                }
                this.seekNextTarget();
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
            }
        }
    }

    private DamageSource getArrowDamageSource(Entity owner, Entity target) {
        DamageSource damageSource;
        if (owner == null) {
            damageSource = target.damageSources().arrow(this, this);
        } else {
            damageSource = target.damageSources().arrow(this, owner);
            if (owner instanceof LivingEntity livingEntity) {
                livingEntity.setLastHurtMob(target);
            }
        }
        if (owner != null && PROJECTILE_ANTI_IMMUNE_ENTITIES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString())) {
            damageSource = ModDamageTypes.causeRandomDamage(owner);
        }
        return damageSource;
    }

    private double getBaseTraceDamage() {
        return this.getPickupItemStackOrigin().isEmpty() ? 2.0D : Float.POSITIVE_INFINITY;
    }

    private boolean damageEntity(Entity entity, DamageSource source, float amount) {
        if (level() instanceof ServerLevel serverLevel) {
            return entity.hurtServer(serverLevel, source, amount);
        }
        return entity.hurtOrSimulate(source, amount);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        this.seekNextTarget();
        this.setInGround(false);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPECTRAL_TIME, 0);
        builder.define(JUMP_COUNT, 0);
    }


    @Override
    public void addAdditionalSaveData(@NotNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.getSpectralTime() > 0) {
            output.putInt("spectral_time", this.entityData.get(SPECTRAL_TIME));
        }

        if (this.getJumpCount() > 0) {
            output.putInt("jump_count", this.entityData.get(JUMP_COUNT));
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSpectral(input.getIntOr("spectral_time", 0));
        this.setJumpCount(input.getIntOr("jump_count", 0));
    }

    @Override
    public void doPostHurtEffects(@NotNull LivingEntity livingEntity) {
        super.doPostHurtEffects(livingEntity);
        int spectralTime = this.entityData.get(SPECTRAL_TIME);
        if (spectralTime > 0) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, spectralTime, 0));
        }

    }

    public void seekNextTarget() {
        if (this.getJumpCount() <= 16 && this.isCritArrow()) {
            if (this.seekOrigin == null) {
                this.seekOrigin = this.position();
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                TargetingConditions conditions = TargetingConditions.forCombat()
                        .selector((living, level) -> {
                            // 排除玩家实体
                            return !(living instanceof Player) &&
                                    living.hasLineOfSight(this);
                        });
                this.homingTarget = serverLevel.getNearestEntity(LivingEntity.class, conditions, owner instanceof LivingEntity ? (LivingEntity) owner : null, this.seekOrigin.x, this.seekOrigin.y, this.seekOrigin.z, this.getBoundingBox().inflate(64.0D));
                if (this.homingTarget != null) {
                    Vec3 targetPos = this.homingTarget.getEyePosition();
                    double x = targetPos.x - this.getX();
                    double y = targetPos.y - this.getY();
                    double z = targetPos.z - this.getZ();
                    this.shoot(x, y, z, 3.0F, 0.0F);
                    this.setJumpCount(this.getJumpCount() + 1);
                    this.homingTime = 0;
                } else {
                    this.destroyArrow();
                }

            }
        } else {
            this.destroyArrow();
        }
    }

    private void updateHoming() {
        if (this.homingTarget != null) {
            if (this.homingTime++ > 60) {
                this.destroyArrow();
            } else if (!this.homingTarget.isDeadOrDying() && !this.homingTarget.isRemoved()) {
                Vec3 targetPos = this.homingTarget.getEyePosition();
                if (targetPos.distanceToSqr(this.position()) >= 4.0D) {
                    double x = targetPos.x - this.getX();
                    double y = targetPos.y - this.getY();
                    double z = targetPos.z - this.getZ();
                    this.shoot(x, y, z, 3.0F, 0.0F);
                }
            } else {
                this.homingTarget = null;
                this.seekNextTarget();
            }
        }
    }

    private void destroyArrow() {
        Level level1 = this.level();
        if (!level1.isClientSide()) {
            if (level1 instanceof ServerLevel level) {
                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.SMOKE, true, false, this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F, 0.0F, 4.0F, 10);

                if (owner instanceof ServerPlayer player) {
                    player.connection.send(packet);
                }
                level.explode(this.getOwner() == null ? this : this.getOwner(), this.getX(), this.getY(), this.getZ(), 4.0F, Level.ExplosionInteraction.NONE);
            }

            this.discard();
        } else {
            level1.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F, 0.0F);
        }
    }

}
