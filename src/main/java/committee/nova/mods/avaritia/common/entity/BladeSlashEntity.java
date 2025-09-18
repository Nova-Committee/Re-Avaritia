package committee.nova.mods.avaritia.common.entity;

import committee.nova.mods.avaritia.api.util.EntityUtils;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

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
        this(ModEntities.BLADE_SLASH.get(), worldIn);
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
    protected void defineSynchedData() {

    }

    @Override
    protected void doWaterSplashEffect() {

    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);

        if (this.getOwner() instanceof Player player) {
            result.getEntity().hurt(this.damageSources().playerAttack(player), damage);
        } else if (this.getOwner() instanceof LivingEntity livingEntity) {
            result.getEntity().hurt(this.damageSources().mobAttack(livingEntity), damage);
        } else {
            result.getEntity().hurt(this.damageSources().generic(), damage);
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > duration) {
            discard();
        }
        calculateCollision(this.level());
        checkInsideBlocks();
        Vec3 velocity = getDeltaMovement();
        setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
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

        if (blockCollision && !ForgeEventFactory.onProjectileImpact(this, blockResult)) {
            this.onHitBlock(blockResult);
        }
    }

    protected BlockHitResult getBlockHitResult(Level world, Vec3 startPos, Vec3 endPos) {
        return world.clip(new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
    }

    protected void hitEntities(Level world, Vec3 startPos, Vec3 endPos) {
        EntityUtils.findHitEntities(world, this, startPos, endPos, this::canHitEntity)
                .filter(result -> !ForgeEventFactory.onProjectileImpact(this, result))
                .forEach(this::onHitEntity);
    }
}
