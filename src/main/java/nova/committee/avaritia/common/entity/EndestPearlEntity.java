package nova.committee.avaritia.common.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import nova.committee.avaritia.init.registry.ModEntities;
import nova.committee.avaritia.init.registry.ModItems;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/3 0:25
 * Version: 1.0
 */
public class EndestPearlEntity extends ThrowableItemProjectile {
    public EndestPearlEntity(EntityType<? extends ThrowableItemProjectile> p_37442_, Level p_37443_) {
        super(p_37442_, p_37443_);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.endest_pearl;
    }


    private ParticleOptions getParticle() {
        ItemStack itemstack = this.getItemRaw();
        return (ParticleOptions) (itemstack.isEmpty() ? ParticleTypes.PORTAL : new ItemParticleOption(ParticleTypes.ITEM, itemstack));
    }

    @Override
    public void handleEntityEvent(byte p_37402_) {
        if (p_37402_ == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for (int i = 0; i < 8; ++i) {
                this.level.addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), random.nextGaussian() * 3, random.nextGaussian() * 3, random.nextGaussian() * 3);
            }
        }

    }

    @Override
    protected void onHitEntity(EntityHitResult pos) {
        super.onHitEntity(pos);

        if (pos.getEntity() != null) {
            pos.getEntity().hurt(DamageSource.thrown(this, getOwner()), 0.0F);
        }

        if (!level.isClientSide) {
            //this.worldObj.createExplosion(this, pos.hitVec.xCoord, pos.hitVec.yCoord, pos.hitVec.zCoord, 4.0f, true);

            Entity ent = ModEntities.GapingVoid.get().create(level);
            Direction dir = pos.getEntity().getDirection();
            Vec3 offset = Vec3.ZERO;
            if (pos.getEntity() != null) {
                offset = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            }
            ent.moveTo(getX() + offset.x * 0.25, getY() + offset.y * 0.25, getZ() + offset.z * 0.25, getYRot(), 0.0F);
            level.addFreshEntity(ent);

            remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void onHit(HitResult p_37406_) {
        super.onHit(p_37406_);
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }

    }
}
