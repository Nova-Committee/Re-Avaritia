package committee.nova.mods.avaritia.common.entity.arrow;

import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;


import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / TraceArrowEntity
 * @author cnlimiter
 * CreateTime: 2023/9/23 16:34
 * Description:
 */

public class TraceArrowEntity extends Arrow {
    private static final int MAX_JUMP_COUNT = 16;
    private static final int HOMING_TIMEOUT = 60;
    private static final double HOMING_RANGE = 64.0D;
    private static final EntityDataAccessor<Integer> SPECTRAL_TIME = SynchedEntityData.defineId(TraceArrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> JUMP_COUNT = SynchedEntityData.defineId(TraceArrowEntity.class, EntityDataSerializers.INT);
    private LivingEntity homingTarget;
    private int homingTime;

    public TraceArrowEntity(EntityType<? extends Arrow> entityType, Level world) {
        super(entityType, world);
        this.homingTarget = null;
        this.homingTime = 0;
    }

    public TraceArrowEntity(Level world, Entity pShooter, double xPos, double yPos, double zPos) {
        this(ModEntities.TRACE_ARROW.get(), world);
        this.setOwner(pShooter);
        this.setPos(xPos, yPos, zPos);
    }

    public TraceArrowEntity(Level world, Entity pShooter) {
        this(world, pShooter, pShooter.getX(), pShooter.getEyeY() - (double) 0.1F, pShooter.getZ());
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
        if (this.homingTarget == null && canSeekNextTarget()) {
            trySeekNextTarget();
        }
        this.updateHoming();
        super.tick();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        if (pResult.getEntity() instanceof LivingEntity livingEntity)
            livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false,false));
        ToolUtils.infinityTraceArrowDamage(level(), pResult, this);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        BlockState blockstate = this.level().getBlockState(hitResult.getBlockPos());
        blockstate.onProjectileHit(this.level(), blockstate, hitResult, this);
        this.seekNextTarget();
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
        if (canSeekNextTarget()) {
            if (!trySeekNextTarget()) {
                this.destroyArrow();
            }
        } else {
            this.destroyArrow();
        }
    }

    private void updateHoming() {
        if (this.homingTarget != null) {
            if (this.homingTime++ > HOMING_TIMEOUT) {
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

    private boolean canSeekNextTarget() {
        return this.getJumpCount() <= MAX_JUMP_COUNT && this.isCritArrow() && !this.level().isClientSide();
    }

    private boolean trySeekNextTarget() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        Entity owner = this.traceOwner();
        LivingEntity source = owner instanceof LivingEntity livingOwner ? livingOwner : null;
        Vec3 origin = this.position();
        TargetingConditions conditions = TargetingConditions.forCombat()
                .selector((living, level) -> living != owner
                        && !(living instanceof Player)
                        && living.isAlive()
                        && !living.isRemoved()
                        && living.hasLineOfSight(this));

        this.homingTarget = serverLevel.getNearestEntity(LivingEntity.class, conditions, source, origin.x, origin.y, origin.z, this.getBoundingBox().inflate(HOMING_RANGE));
        if (this.homingTarget == null) {
            return false;
        }

        Vec3 targetPos = this.homingTarget.getEyePosition();
        this.shoot(targetPos.x - this.getX(), targetPos.y - this.getY(), targetPos.z - this.getZ(), 3.0F, 0.0F);
        this.setJumpCount(this.getJumpCount() + 1);
        this.homingTime = 0;
        return true;
    }

    private void destroyArrow() {
        Level level1 = this.level();
        if (!level1.isClientSide()) {
            if (level1 instanceof ServerLevel level) {
                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.SMOKE, true, false, this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F, 0.0F, 4.0F, 10);

                Entity owner = this.traceOwner();
                if (owner instanceof ServerPlayer player) {
                    player.connection.send(packet);
                }
                level.explode(owner, this.getX(), this.getY(), this.getZ(), 4.0F, Level.ExplosionInteraction.NONE);
            }

            this.discard();
        } else {
            level1.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F, 0.0F);
        }
    }

    private Entity traceOwner() {
        Entity owner = this.getOwner();
        return owner == null ? this : owner;
    }

}
