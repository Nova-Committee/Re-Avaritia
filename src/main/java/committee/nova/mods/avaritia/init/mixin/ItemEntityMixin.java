package committee.nova.mods.avaritia.init.mixin;

import committee.nova.mods.avaritia.api.init.event.ModEventFactory;
import committee.nova.mods.avaritia.init.iface.ILifeSpan;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * ItemEntityMixin
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午2:58
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements ILifeSpan {
    @Unique
    public int lifespan = 6000;

    @Shadow public abstract ItemStack getItem();

    @Shadow public int pickupDelay;
    @Shadow @Nullable private UUID target;
    @Shadow public int age;
    @Unique
    ItemEntity itemEntity = (ItemEntity) (Object) this;

    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/item/ItemEntity;)V",
            at = @At(value = "TAIL"))
    private void init1(ItemEntity itemEntity, CallbackInfo ci){
        this.lifespan = itemEntity.getLifeSpan();
    }

    @Override
    public int getLifeSpan() {
        return lifespan;
    }

    @Override
    public void setLifeSpan(int lifespan) {
        this.lifespan = lifespan;
    }

    @Inject(
            method = "playerTouch",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I", shift = At.Shift.AFTER),
            cancellable = true)
    private void playerTouch1(Player player, CallbackInfo ci) {
        ItemStack itemstack = this.getItem();
        Item item = itemstack.getItem();
        int i = itemstack.getCount();
        int hook = ModEventFactory.onItemPickup(itemEntity, player);
        if (hook < 0) ci.cancel();
        ItemStack copy = this.getItem().copy();
        if (this.pickupDelay == 0 && (this.target == null || this.target.equals(player.getUUID())) && (hook == 1 || i <= 0 || player.getInventory().add(itemstack))) {
            i = copy.getCount() - itemstack.getCount();
            copy.setCount(i);
            ModEventFactory.firePlayerItemPickupEvent(player, itemEntity, copy);
            player.take(this, i);
            if (itemstack.isEmpty()) {
                this.discard();
                itemstack.setCount(i);
            }

            player.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
            player.onItemPickup(itemEntity);
        }
        ci.cancel();
    }

    @Inject(
            method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;level()Lnet/minecraft/world/level/Level;",
                    shift = At.Shift.BEFORE,
                    ordinal = 5
            )
    )
    private void tick1(CallbackInfo ci){
        ItemStack item = this.getItem();
        if (!this.level().isClientSide && this.age >= lifespan) {
            int hook = ModEventFactory.onItemExpire(itemEntity, item);
            if (hook < 0) {
                this.discard();
            } else {
                this.lifespan += hook;
            }
        }
    }

    @Inject(
            method = "addAdditionalSaveData",
            at = @At(value = "HEAD")
    )
    private void addTag(CompoundTag compoundTag, CallbackInfo ci){
        compoundTag.putInt("Lifespan", this.lifespan);
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At(value = "HEAD")
    )
    private void readTag(CompoundTag compoundTag, CallbackInfo ci){
        if (compoundTag.contains("Lifespan")) {
            this.lifespan = compoundTag.getInt("Lifespan");
        }
    }
}
