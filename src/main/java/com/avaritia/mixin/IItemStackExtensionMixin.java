package com.avaritia.mixin;

import com.avaritia.api.iface.item.InitEnchantItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Mixin(IItemStackExtension.class)
public interface IItemStackExtensionMixin {

    @Inject(method = "getAllEnchantments", at = @At("RETURN"), cancellable = true)
    default void onGetAllEnchantments(HolderLookup.RegistryLookup<Enchantment> lookup, CallbackInfoReturnable<ItemEnchantments> cir) {

        ItemStack stack = getSelf((IItemStackExtension) this);
        ItemEnchantments originalEnchantments = cir.getReturnValue();

        if (stack.getItem() instanceof InitEnchantItem initEnchantItem) {
            var mutableEnchantments = new ItemEnchantments.Mutable(originalEnchantments);

            lookup.listElements().forEach(holder -> {
                int level = initEnchantItem.getInitEnchantLevel(stack, holder);
                if (level > 0) {
                    mutableEnchantments.set(holder, level);
                }
            });

            cir.setReturnValue(mutableEnchantments.toImmutable());
        }
    }
    private static ItemStack getSelf(IItemStackExtension instance) {
        try {
            Method selfMethod = IItemStackExtension.class.getDeclaredMethod("self");
            selfMethod.setAccessible(true);
            return (ItemStack) selfMethod.invoke(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke self() method", e);
        }
    }
}
