package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.capability.RingStorageProvider;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class DimensionItemStackMixin {
    @Inject(method = "of", at = @At("RETURN"))
    private static void avaritia$readLegacyRing(CompoundTag data, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if (stack.getItem() instanceof NeutronRingItem && data.contains("ForgeCaps", Tag.TAG_COMPOUND)
                && !stack.getOrCreateTag().contains(RingStorageProvider.LEGACY_STORAGE)) {
            stack.getOrCreateTag().put(RingStorageProvider.LEGACY_STORAGE, data.getCompound("ForgeCaps").copy());
        }
    }
}
