package committee.nova.mods.avaritia.common.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:33
 * Version: 1.0
 */
public class ImmortalItemEntity extends ItemEntity {


    public ImmortalItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        this.pickupDelay = 5;
        this.lifespan = 3600;

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
    public boolean hurt(@NotNull DamageSource source, float p_70097_2_) {
        return source == this.damageSources().fellOutOfWorld();
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
