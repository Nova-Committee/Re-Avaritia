package committee.nova.mods.avaritia.common.entity.arrow;

import committee.nova.mods.avaritia.init.registry.ModDamageTypes;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModMobEffects;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class ExplosionsArrowEntity extends Arrow {
    public ExplosionsArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    public ExplosionsArrowEntity(Level world, Entity pShooter, double xPos, double yPos, double zPos) {
        this(ModEntities.EXPLOSIONS_ARROW.get(), world);
        this.setOwner(pShooter);
        this.setPos(xPos, yPos, zPos);
    }

    public ExplosionsArrowEntity(Level world, Entity pShooter) {
        this(world, pShooter, pShooter.getX(), pShooter.getEyeY() - (double) 0.1F, pShooter.getZ());
        if (pShooter instanceof Player) {
            this.pickup = Pickup.DISALLOWED;
        }
    }

    public ExplosionsArrowEntity(Entity pShooter) {
        this(pShooter.level(), pShooter);
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        var pos = result.getBlockPos();
        if (getOwner() != null) {
            this.level().explode(this, this.getX(), this.getY(0.0625D), this.getZ(), 8.0F, Level.ExplosionInteraction.MOB);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level().getEntitiesOfClass(LivingEntity.class, AABB.ofSize(pos.getCenter(), 10.0D, 10.0D, 10.0D), (entityx) -> !entityx.isSpectator())
                    .forEach(entityx -> entityx.addEffect(new MobEffectInstance(ModMobEffects.BURNING.get(), -1)));
        }
        this.remove(RemovalReason.KILLED);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        Entity entity = result.getEntity();
        final float HEAVEN_ARROW_DAMAGE = 100F;
        if (getOwner() != null) {
            if (entity == getOwner()) {
                return;
            }
            entity.hurt(ModDamageTypes.causeRandomDamage(level(), this.getOwner()), HEAVEN_ARROW_DAMAGE);
            level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(10.0D, 10.0D, 10.0D), (entityx) -> !entityx.isSpectator())
                    .forEach(entityx -> entityx.addEffect(new MobEffectInstance(ModMobEffects.BURNING.get(), -1)));
        }
        this.remove(RemovalReason.KILLED);
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
