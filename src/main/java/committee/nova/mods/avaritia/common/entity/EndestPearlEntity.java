package committee.nova.mods.avaritia.common.entity;

import committee.nova.mods.avaritia.init.registry.ModEntityTypes;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 0:25
 * Version: 1.0
 */
public class EndestPearlEntity extends ThrowableItemProjectile {
    private LivingEntity shooter;

    public EndestPearlEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public EndestPearlEntity(Level level, double x, double y, double z) {
        this(ModEntityTypes.ENDER_PEARL.get(), level);
        setPos(x, y, z);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.endest_pearl.get();
    }

    public void setShooter(LivingEntity shooter) {
        this.shooter = shooter;
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 3) {
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pos) {
        super.onHitEntity(pos);
        Entity entity = pos.getEntity();

        damageEntity(entity, this.damageSources().thrown(this, getOwner()), 0.0F);

        if (!level().isClientSide()) {
            GapingVoidEntity ent;
            if (shooter != null) {
                ent = new GapingVoidEntity(level(), shooter);
            } else ent = new GapingVoidEntity(level());

            Direction dir = entity.getDirection();
            Vec3 offset;
            offset = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            if (shooter != null) {
                ent.setUser(shooter);
            }
            ent.moveOrInterpolateTo(new Vec3(entity.getX() + offset.x * 0.25, entity.getY() + offset.y * 0.25, entity.getZ() + offset.z * 0.25), entity.getYRot(), 0.0F);
            level().addFreshEntity(ent);

            remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        BlockPos pos = result.getBlockPos();

        if (!level().isClientSide()) {

            GapingVoidEntity ent;
            if (shooter != null) {
                ent = new GapingVoidEntity(level(), shooter);

            } else ent = new GapingVoidEntity(level());
            Direction dir = result.getDirection();
            Vec3 offset;
            offset = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            if (shooter != null) {
                ent.setUser(shooter);
            }
            ent.moveOrInterpolateTo(new Vec3(pos.getX() + offset.x * 0.25, pos.getY() + offset.y * 0.25, pos.getZ() + offset.z * 0.25), getYRot(), 0.0F);
            level().addFreshEntity(ent);

            remove(RemovalReason.KILLED);
        }
    }

    private void damageEntity(Entity entity, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            entity.hurtServer(serverLevel, source, amount);
        } else {
            entity.hurtOrSimulate(source, amount);
        }
    }
}
