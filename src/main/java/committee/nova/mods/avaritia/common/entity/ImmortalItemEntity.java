package committee.nova.mods.avaritia.common.entity;


import committee.nova.mods.avaritia.init.config.ModConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter,Cu6
 * Date: 2022/3/31 14:33
 * Version: 1.0
 */
public class ImmortalItemEntity extends ItemEntity {
    public ImmortalItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        this.pickupDelay = 5;
        this.lifespan = 3600;
        this.setUnlimitedLifetime();
    }

    public static ImmortalItemEntity create(EntityType<ImmortalItemEntity> type, Level level, double x, double y, double z, ItemStack itemStack) {
        ImmortalItemEntity entity = type.create(level);
        if (entity != null) {
            entity.setPos(x, y, z);
            entity.setItem(itemStack);
        }
        return entity;

    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return source == this.damageSources().fellOutOfWorld();
    }

    @Override
    public void tick() {
        super.tick();


        if (!this.level().isClientSide) {

            Player targetPlayer = this.level().getNearestPlayer(this, ModConfig.immortalItemEntityRange.get());


            if (targetPlayer != null) {

                Vec3 direction = new Vec3(
                        targetPlayer.getX() - this.getX(),
                        targetPlayer.getY() + targetPlayer.getEyeHeight() - this.getY(),
                        targetPlayer.getZ() - this.getZ()
                ).normalize();


                this.setDeltaMovement(direction.scale(ModConfig.immortalItemEntitySpeed.get()));


                this.setDeltaMovement(this.getDeltaMovement().add(0, -0.02D, 0));
            }
        }
    }

    @Override
    public void remove(@NotNull RemovalReason pReason) {
        super.remove(pReason);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }
}
