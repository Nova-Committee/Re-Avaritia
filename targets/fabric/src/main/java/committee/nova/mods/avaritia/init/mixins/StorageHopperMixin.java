package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import committee.nova.mods.avaritia.core.io.NativeInventory;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Virtual bulk slots cannot use vanilla's mutable stack snapshot/rollback algorithm. */
@Mixin(HopperBlockEntity.class)
public abstract class StorageHopperMixin {
    @Inject(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private static void avaritia$insertNative(Container source, Container target, ItemStack stack, Direction side, CallbackInfoReturnable<ItemStack> cir) {
        if (!(target instanceof NativeInventory inventory)) return;
        ItemHandler handler = inventory.getItemHandler(side);
        ItemStack remaining = stack;
        if (handler != null) {
            for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) remaining = handler.insertItem(slot, remaining, false);
        }
        if (remaining.getCount() != stack.getCount()) target.setChanged();
        cir.setReturnValue(remaining);
    }

    @Inject(method = "tryTakeInItemFromSlot", at = @At("HEAD"), cancellable = true)
    private static void avaritia$extractNative(Hopper hopper, Container source, int slot, Direction side, CallbackInfoReturnable<Boolean> cir) {
        if (!(source instanceof NativeInventory inventory)) return;
        ItemHandler handler = inventory.getItemHandler(side);
        if (handler == null || slot < 0 || slot >= handler.getSlots()) { cir.setReturnValue(false); return; }
        ItemStack available = handler.extractItem(slot, 1, true);
        if (available.isEmpty() || !source.canTakeItem(hopper, slot, available)
                || source instanceof WorldlyContainer sided && !sided.canTakeItemThroughFace(slot, available, side)) {
            cir.setReturnValue(false);
            return;
        }
        ItemStack remaining = HopperBlockEntity.addItem(source, hopper, available, null);
        if (!remaining.isEmpty()) { cir.setReturnValue(false); return; }
        handler.extractItem(slot, 1, false);
        source.setChanged();
        cir.setReturnValue(true);
    }
}
