package committee.nova.mods.avaritia.common.entity;

import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static net.minecraftforge.event.ForgeEventFactory.firePlayerItemPickupEvent;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 14:33
 * Version: 1.0
 */
public class ImmortalItemEntity extends ItemEntity {


    public ImmortalItemEntity(EntityType<? extends ItemEntity> type, Level p_i50217_2_) {
        super(type, p_i50217_2_);
        this.setPickUpDelay(20);
    }

    public static ImmortalItemEntity create(EntityType<? extends ItemEntity> type, Level level, double x, double y, double z, ItemStack itemStack) {
        ImmortalItemEntity entity = (ImmortalItemEntity) type.create(level);
        if (entity != null) {
            entity.setPos(x, y, z);
            entity.setItem(itemStack);
        }
        return entity;

    }


    @Override
    public boolean hurt(DamageSource source, float p_70097_2_) {
        return source == this.damageSources().fellOutOfWorld();
    }


    @Override
    public void remove(RemovalReason pReason) {
        if (this.getAge() < 2400)
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

    @Override
    public void playerTouch(@NotNull Player pEntity) {
        if (!this.getCommandSenderWorld().isClientSide) {
            if (this.pickupDelay > 0) return;
            ItemStack itemstack = this.getItem();
            Item item = itemstack.getItem();
            int i = itemstack.getCount();

            int hook = net.minecraftforge.event.ForgeEventFactory.onItemPickup(this, pEntity);
            if (hook < 0) return;

            ItemStack copy = itemstack.copy();
            if (this.pickupDelay == 0 &&
                    (this.getOwner() == null || lifespan - this.getAge() <= 200 || this.getOwner().equals(pEntity.getUUID()))
                    && (hook == 1 || i <= 0 || pEntity.getInventory().add(itemstack))) {
                copy.setCount(copy.getCount() - getItem().getCount());
                firePlayerItemPickupEvent(pEntity, this, copy);
                pEntity.take(this, i);
                if (itemstack.isEmpty()) {
                    this.age = 2400;
                    this.remove(RemovalReason.KILLED);
                    itemstack.setCount(i);
                }

                pEntity.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
                pEntity.onItemPickup(this);
            }
        }
    }


}
