package committee.nova.mods.avaritia.common.entity;

import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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
import org.jetbrains.annotations.NotNull;

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


    public EndestPearlEntity(Level level, double x, double y, double z) {
        this(ModEntities.ENDER_PEARL.get(), level);
        setPos(x, y, z);
    }

    public EndestPearlEntity(Level level, LivingEntity shooter) {
        this(ModEntities.ENDER_PEARL.get(), level);
        this.setShooter(shooter);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.endest_pearl.get();
    }


    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
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
                this.getCommandSenderWorld().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
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

        if (!getCommandSenderWorld().isClientSide) {
            GapingVoidEntity ent;
            if (shooter != null) {
                ent = new GapingVoidEntity(getCommandSenderWorld(), shooter);

            } else ent = new GapingVoidEntity(getCommandSenderWorld());

            Direction dir = entity.getDirection();
            Vec3 offset = Vec3.ZERO;
            if (dir != null) {
                offset = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            }
            if (shooter != null) {
                ent.setUser(shooter);
            }
            ent.moveTo(entity.getX() + offset.x * 0.25, entity.getY() + offset.y * 0.25, entity.getZ() + offset.z * 0.25, entity.getYRot(), 0.0F);
            getCommandSenderWorld().addFreshEntity(ent);

            remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        BlockPos pos = result.getBlockPos();

        if (!getCommandSenderWorld().isClientSide) {

            GapingVoidEntity ent;
            if (shooter != null) {
                ent = new GapingVoidEntity(getCommandSenderWorld(), shooter);

            } else ent = new GapingVoidEntity(getCommandSenderWorld());
            Direction dir = result.getDirection();
            Vec3 offset = Vec3.ZERO;
            if (dir != null) {
                offset = new Vec3(dir.getStepX(), dir.getStepY(), dir.getStepZ());
            }
            if (shooter != null) {
                ent.setUser(shooter);
            }
            ent.moveTo(pos.getX() + offset.x * 0.25, pos.getY() + offset.y * 0.25, pos.getZ() + offset.z * 0.25, getYRot(), 0.0F);
            getCommandSenderWorld().addFreshEntity(ent);


            remove(RemovalReason.KILLED);
        }
    }
}
