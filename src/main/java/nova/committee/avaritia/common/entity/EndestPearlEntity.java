package nova.committee.avaritia.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import nova.committee.avaritia.init.registry.ModEntities;
import nova.committee.avaritia.init.registry.ModItems;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 0:25
 * Version: 1.0
 */
public class EndestPearlEntity extends ThrowableItemProjectile {
    private LivingEntity shooter;

    public EndestPearlEntity(EntityType<? extends ThrowableItemProjectile> p_37442_, Level p_37443_) {
        super(p_37442_, p_37443_);
    }


    public static EndestPearlEntity create(Level level, double x, double y, double z) {
        EndestPearlEntity entity = new EndestPearlEntity(ModEntities.EnderPearl.get(), level);
        entity.setPos(x, y, z);
        return entity;
    }

    public static EndestPearlEntity create(Level level, LivingEntity shooter) {
        EndestPearlEntity entity = new EndestPearlEntity(ModEntities.EnderPearl.get(), level);
        entity.setShooter(shooter);
        return entity;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.endest_pearl;
    }


    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private ParticleOptions getParticle() {
        ItemStack itemstack = this.getItemRaw();
        return itemstack.isEmpty() ? ParticleTypes.PORTAL : new ItemParticleOption(ParticleTypes.ITEM, itemstack);
    }

    public void setShooter(LivingEntity shooter) {
        this.shooter = shooter;
    }


    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for (int i = 0; i < 8; ++i) {
                this.level.addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    @Override
    protected void onHitEntity(EntityHitResult pos) {
        super.onHitEntity(pos);
        Entity entity = pos.getEntity();

        if (entity != null) {
            entity.hurt(DamageSource.thrown(this, getOwner()), 0.0F);
        }

        if (!level.isClientSide) {
            GapingVoidEntity ent;
            if (shooter != null) {
                ent = GapingVoidEntity.create(level, shooter);

            } else ent = GapingVoidEntity.create(level);

            Direction dir = entity.getDirection();
            Vec3 offset = Vec3.ZERO;
            if (dir != null) {
                offset = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            }
            if (shooter != null) {
                ent.setUser(shooter);
            }
            ent.moveTo(entity.getX() + offset.x * 0.25, entity.getY() + offset.y * 0.25, entity.getZ() + offset.z * 0.25, entity.getYRot(), 0.0F);
            level.addFreshEntity(ent);

            remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        BlockPos pos = result.getBlockPos();

        if (!level.isClientSide) {

            GapingVoidEntity ent;
            if (shooter != null) {
                ent = GapingVoidEntity.create(level, shooter);

            } else ent = GapingVoidEntity.create(level);
            Direction dir = result.getDirection();
            Vec3 offset = Vec3.ZERO;
            if (dir != null) {
                offset = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            }
            if (shooter != null) {
                ent.setUser(shooter);
            }
            ent.moveTo(pos.getX() + offset.x * 0.25, pos.getY() + offset.y * 0.25, pos.getZ() + offset.z * 0.25, getYRot(), 0.0F);
            level.addFreshEntity(ent);


            remove(RemovalReason.KILLED);
        }
    }
}
