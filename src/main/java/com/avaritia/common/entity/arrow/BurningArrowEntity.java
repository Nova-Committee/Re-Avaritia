package com.avaritia.common.entity.arrow;

import com.avaritia.init.registry.ModEntityTypes;
import com.avaritia.init.registry.ModMobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class BurningArrowEntity extends Arrow {
    public BurningArrowEntity(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    public BurningArrowEntity(Level world, Entity pShooter, double xPos, double yPos, double zPos) {
        this(ModEntityTypes.BURNING_ARROW.get(), world);
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
    protected void onHitEntity(@NotNull EntityHitResult result) {
        //super.onHitEntity(result);
        Entity owner = this.getOwner();
        if (!this.level().isClientSide() && owner instanceof ServerPlayer player && result.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance((Holder<MobEffect>) ModMobEffects.BURNING, 1200));
        }
    }


    @Override
    protected float getWaterInertia() {
        return 1.0F;
    }

    @Override
    public @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

}
