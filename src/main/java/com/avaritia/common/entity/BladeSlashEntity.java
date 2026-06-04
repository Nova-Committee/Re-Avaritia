package com.avaritia.common.entity;

import com.avaritia.init.config.ModConfig;
import com.avaritia.init.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/16 01:19
 * @Description: from <a href="https://github.com/CoFH/RedstoneArsenal/blob/1.20.x/src/main/java/cofh/redstonearsenal/common/entity/FluxSlash.java">...</a>
 */
public class BladeSlashEntity extends Projectile {
    public static float defaultSpeed = 2.0F;
    public final float zRot;
    public float damage = ModConfig.bladeSlashDamage.get();
    public int duration = ModConfig.bladeSlashRadius.get();

    public BladeSlashEntity(EntityType<? extends Projectile> type, Level worldIn) {
        super(type, worldIn);
        zRot = (worldIn.getRandom().nextFloat() - 0.5F) * 50;
    }

    public BladeSlashEntity(Level worldIn, double x, double y, double z) {
        this(ModEntityTypes.BLADE_SLASH.get(), worldIn);
        this.setPos(x, y, z);
    }

    public BladeSlashEntity(Level worldIn, LivingEntity livingEntityIn) {
        this(worldIn, livingEntityIn.getX(), 0.7 * livingEntityIn.getEyeY() + 0.3 * livingEntityIn.getY(), livingEntityIn.getZ());
        this.setOwner(livingEntityIn);
        this.shootFromRotation(livingEntityIn, livingEntityIn.getXRot(), livingEntityIn.getYRot(), 0.0F, defaultSpeed, 0.5F);
    }

    public BladeSlashEntity(Level worldIn, LivingEntity livingEntityIn, int damageModifier) {
        this(worldIn, livingEntityIn);
        this.damage += damageModifier;
    }

    public BladeSlashEntity(Level worldIn, LivingEntity livingEntityIn, int damageModifier, int durationModifier) {
        this(worldIn, livingEntityIn, damageModifier);
        this.duration += durationModifier;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {

    }


    @Override
    protected void doWaterSplashEffect() {

    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);

        if (this.getOwner() instanceof Player player) {
            damageEntity(result.getEntity(), this.damageSources().playerAttack(player), damage);
        } else if (this.getOwner() instanceof LivingEntity livingEntity) {
            damageEntity(result.getEntity(), this.damageSources().mobAttack(livingEntity), damage);
        } else {
            damageEntity(result.getEntity(), this.damageSources().generic(), damage);
        }
    }

    private void damageEntity(net.minecraft.world.entity.Entity entity, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            entity.hurtServer(serverLevel, source, amount);
        } else {
            entity.hurtOrSimulate(source, amount);
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);

        if (this.level() instanceof ServerLevel serverLevel) {

            serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    15,
                    0.5, 0.5, 0.5,
                    0.1
            );
        }
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > duration) {
            discard();
        }
        calculateCollision(this.level());
        Vec3 velocity = getDeltaMovement();
        Vec3 start = this.position();
        Vec3 end = start.add(velocity);
        this.applyEffectsFromBlocks(start, end);
        setPos(end.x, end.y, end.z);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_70112_1_) {
        double d0 = this.getBoundingBox().getSize() * 10.0D;
        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }
        d0 = d0 * 64.0D * getViewScale();
        return p_70112_1_ < d0 * d0;
    }

    protected void calculateCollision(Level world) {
        Vec3 start = this.position();
        Vec3 end = start.add(this.getDeltaMovement());
        BlockHitResult blockResult = this.getBlockHitResult(world, start, end);
        boolean blockCollision = false;
        if (blockResult.getType() != HitResult.Type.MISS) {
            end = blockResult.getLocation();
            blockCollision = true;
        }
        this.hitEntities(this.level(), start, end);

        if (blockCollision) {
            this.onHitBlock(blockResult);
        }
    }

    protected BlockHitResult getBlockHitResult(Level world, Vec3 startPos, Vec3 endPos) {
        return world.clip(new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
    }

    protected void hitEntities(Level world, Vec3 startPos, Vec3 endPos) {
        Vec3 padding = new Vec3(this.getBbWidth() * 0.5, this.getBbHeight() * 0.5, this.getBbWidth() * 0.5);
        AABB searchArea = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.5D);
        world.getEntities(this, searchArea, this::canHitEntity).stream()
                .map(entity -> entity.getBoundingBox()
                        .inflate(padding.x(), padding.y(), padding.z())
                        .clip(startPos, endPos)
                        .map(hitPos -> new EntityHitResult(entity, hitPos))
                        .orElse(null))
                .filter(Objects::nonNull)
                .forEach(this::onHitEntity);
    }
}
