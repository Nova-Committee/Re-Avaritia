package committee.nova.mods.avaritia.common.entity.arrow;

import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class BurningArrowEntity extends Arrow {
    public BurningArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    public BurningArrowEntity(Level world, Entity pShooter, double xPos, double yPos, double zPos) {
        this(ModEntities.BURNING_ARROW.get(), world);
        this.setOwner(pShooter);
        this.setPos(xPos, yPos, zPos);
    }

    public BurningArrowEntity(Level world, Entity pShooter) {
        this(world, pShooter, pShooter.getX(), pShooter.getEyeY() - (double) 0.1F, pShooter.getZ());
        if (pShooter instanceof Player) {
            this.pickup = Pickup.DISALLOWED;
        }
    }

    public BurningArrowEntity(Entity pShooter) {
        this(pShooter.level(), pShooter);
    }
    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        this.discard();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            BlockPos pos = result.getBlockPos();
            if (owner instanceof ServerPlayer player) {
                var entities = level().getEntitiesOfClass(LivingEntity.class, new AABB(pos.offset(-4, -4, -4), pos.offset(4, 4, 4)), livingEntity -> {
                    return !livingEntity.isSpectator() && livingEntity.isAlive() && !livingEntity.equals(player);
                });
                entities.forEach(entity -> {
                    entity.addEffect(new MobEffectInstance(ModMobEffects.BURNING.get(), 1200, 3));
                });
            }
        }

    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            var pos = result.getEntity().blockPosition();
            if (owner instanceof Player player) {
                var entities = level().getEntitiesOfClass(LivingEntity.class, new AABB(pos.offset(-4, -4, -4), pos.offset(4, 4, 4)), livingEntity -> {
                    return !livingEntity.isSpectator() && livingEntity.isAlive() && !livingEntity.equals(player);
                });
                entities.forEach(entity -> {
                    entity.addEffect(new MobEffectInstance(ModMobEffects.BURNING.get(), 1200));
                });
            }
        }
    }


    @Override
    protected float getWaterInertia() {
        return 1.0F;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

}
