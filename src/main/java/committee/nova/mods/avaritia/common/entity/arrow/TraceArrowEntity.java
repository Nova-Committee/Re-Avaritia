package committee.nova.mods.avaritia.common.entity.arrow;

import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / TraceArrowEntity
 * Author: cnlimiter
 * CreateTime: 2023/9/23 16:34
 * Description:
 */

public class TraceArrowEntity extends Arrow {
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
        this.updateHoming();
        this.superTick();
        super.tick();
    }

    private void superTick() {
        if (!this.leftOwner) {
            this.leftOwner = this.checkLeftOwner();
        }

        if (!this.level().isClientSide) {
            this.setSharedFlag(6, this.isCurrentlyGlowing());
        }

        this.baseTick();
        boolean flag = this.isNoPhysics();
        Vec3 vector3d = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double f = vector3d.horizontalDistance();
            this.setYRot((float) (Mth.atan2(vector3d.x, vector3d.z) * 57.2957763671875D));
            this.setXRot((float) (Mth.atan2(vector3d.y, f) * 57.2957763671875D));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level().getBlockState(blockpos);
        Vec3 vector3d3;
        if (!blockstate.isAir() && !flag) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
            if (!voxelshape.isEmpty()) {
                vector3d3 = this.position();

                for (AABB axisalignedbb : voxelshape.toAabbs()) {
                    if (axisalignedbb.move(blockpos).contains(vector3d3)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.isInWaterOrRain()) {
            this.clearFire();
        }

        if (this.inGround && !flag) {
            if (this.lastState != blockstate && this.shouldFall()) {
                this.startFalling();
            } else if (!this.level().isClientSide) {
                this.tickDespawn();
            }

            ++this.inGroundTime;
        } else {
            this.inGroundTime = 0;
            Vec3 vector3d2 = this.position();
            vector3d3 = vector3d2.add(vector3d);
            HitResult raytraceresult = this.level().clip(new ClipContext(vector3d2, vector3d3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (raytraceresult.getType() != HitResult.Type.MISS) {
                vector3d3 = raytraceresult.getLocation();
            }

            while (!this.isRemoved()) {
                EntityHitResult entityraytraceresult = this.findHitEntity(vector3d2, vector3d3);
                if (entityraytraceresult != null) {
                    raytraceresult = entityraytraceresult;
                }

                if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult) raytraceresult).getEntity();
                    Entity entity1 = this.getOwner();
                    if (entity instanceof Player && entity1 instanceof Player && !((Player) entity1).canHarmPlayer((Player) entity)) {
                        raytraceresult = null;
                        entityraytraceresult = null;
                    }
                }

                if (raytraceresult != null && raytraceresult.getType() != HitResult.Type.MISS && !flag && !ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                    this.onHit(raytraceresult);
                    this.hasImpulse = true;
                }

                if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
                    break;
                }

                raytraceresult = null;
            }

            vector3d = this.getDeltaMovement();
            double d3 = vector3d.x;
            double d4 = vector3d.y;
            double d0 = vector3d.z;
            if (this.isCritArrow()) {
                for (int i = 0; i < 4; ++i) {
                    this.level().addParticle(ParticleTypes.CRIT, this.getX() + d3 * (double) i / 4.0D, this.getY() + d4 * (double) i / 4.0D, this.getZ() + d0 * (double) i / 4.0D, -d3, -d4 + 0.2D, -d0);
                }
            }

            double d5 = this.getX() + d3;
            double d1 = this.getY() + d4;
            double d2 = this.getZ() + d0;
            double f1 = vector3d.horizontalDistance();
            if (flag) {
                this.setYRot((float) (Mth.atan2(-d3, -d0) * 57.2957763671875D));
            } else {
                this.setYRot((float) (Mth.atan2(d3, d0) * 57.2957763671875D));
            }

            this.setXRot((float) (Mth.atan2(d4, f1) * 57.2957763671875D));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            float f2 = 0.99F;
            float f3 = 0.05F;
            if (this.isInWater()) {
                for (int j = 0; j < 4; ++j) {
                    float f4 = 0.25F;
                    this.level().addParticle(ParticleTypes.BUBBLE, d5 - d3 * f4, d1 - d4 * f4, d2 - d0 * f4, d3, d4, d0);
                }

                f2 = this.getWaterInertia();
            }

            this.setDeltaMovement(vector3d.scale(f2));
            if (!this.isNoGravity() && !flag) {
                Vec3 vector3d4 = this.getDeltaMovement();
                this.setDeltaMovement(vector3d4.x, vector3d4.y - 0.05000000074505806D, vector3d4.z);
            }

            this.setPos(d5, d1, d2);
            this.checkInsideBlocks();
        }

    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        ToolUtils.infinityTraceArrowDamage(pResult, this);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        this.lastState = this.level().getBlockState(hitResult.getBlockPos());
        BlockState blockstate = this.level().getBlockState(hitResult.getBlockPos());
        blockstate.onProjectileHit(this.level(), blockstate, hitResult, this);
        Vec3 vec3 = hitResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale(0.05000000074505806D);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        this.seekNextTarget();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 4.0F, 1.0F);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SPECTRAL_TIME, 0);
        this.entityData.define(JUMP_COUNT, 0);
    }


    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.getSpectralTime() > 0) {
            compound.putInt("spectral_time", this.entityData.get(SPECTRAL_TIME));
        }

        if (this.getJumpCount() > 0) {
            compound.putInt("jump_count", this.entityData.get(JUMP_COUNT));
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("spectral_time")) {
            this.setSpectral(compound.getInt("spectral_time"));
        }
        if (compound.contains("jump_count")) {
            this.setJumpCount(compound.getInt("jump_count"));
        }
    }

    @Override
    public void doPostHurtEffects(@NotNull LivingEntity livingEntity) {
        super.doPostHurtEffects(livingEntity);
        int spectralTime = this.entityData.get(SPECTRAL_TIME);
        if (spectralTime > 0) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, spectralTime, 0));
        }

    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.ON_FIRE) || super.isInvulnerableTo(source);
    }

    public void seekNextTarget() {
        if (this.getJumpCount() <= 16 && this.isCritArrow()) {
            if (this.seekOrigin == null) {
                this.seekOrigin = this.position();
            }

            if (!this.level().isClientSide) {
                TargetingConditions conditions = TargetingConditions.forCombat().selector((living) -> living.hasLineOfSight(this));
                this.homingTarget = this.level().getNearestEntity(LivingEntity.class, conditions, owner instanceof LivingEntity ? (LivingEntity) owner : null, this.seekOrigin.x, this.seekOrigin.y, this.seekOrigin.z, this.getBoundingBox().inflate(64.0D));
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
                    this.hasImpulse = true;
                }
            } else {
                this.homingTarget = null;
                this.seekNextTarget();
            }
        }
    }

    private void destroyArrow() {
        Level level1 = this.level();
        if (!level1.isClientSide) {
            if (level1 instanceof ServerLevel level) {
                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.SMOKE, true, this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F, 0.0F, 4.0F, 10);

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
