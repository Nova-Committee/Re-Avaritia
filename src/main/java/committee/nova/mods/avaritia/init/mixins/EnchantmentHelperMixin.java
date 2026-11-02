package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.iface.InitEnchantItem;
import committee.nova.mods.avaritia.core.io.StorageLifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/11/15 14:43
 * @Description:
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(at = @At("RETURN"), method = "getItemEnchantmentLevel", cancellable = true)
    private static void getItemEnchantmentLevel(Holder<Enchantment> enchantment, ItemInstance stack,
                                                CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() == 0 && stack.typeHolder().value() instanceof InitEnchantItem item) {
            int level = item.getInitEnchantLevel(stack, enchantment);
            if (level != 0) {
                cir.setReturnValue(level);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V")
    private static void runInitEnchantIteration(ItemStack piece, EnchantmentHelper.EnchantmentVisitor method, CallbackInfo ci) {
        if (piece.getItem() instanceof InitEnchantItem item) {
            MinecraftServer server = StorageLifecycle.getServer();
            if (server == null) return;
            ItemEnchantments existing = piece.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach(holder -> {
                int level = item.getInitEnchantLevel(piece, holder);
                if (level > 0 && existing.getLevel(holder) <= 0) {
                    method.accept(holder, level);
                }
            });
        }
    }

    @Inject(at = @At("RETURN"), method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V")
    private static void runInitEnchantIterationInSlot(ItemStack piece, EquipmentSlot slot, LivingEntity owner, EnchantmentHelper.EnchantmentInSlotVisitor method, CallbackInfo ci) {
        if (piece.getItem() instanceof InitEnchantItem item) {
            ItemEnchantments existing = piece.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            EnchantedItemInUse itemInUse = new EnchantedItemInUse(piece, slot, owner);
            owner.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach(holder -> {
                int level = item.getInitEnchantLevel(piece, holder);
                if (level > 0 && existing.getLevel(holder) <= 0 && holder.value().matchingSlot(slot)) {
                    method.accept(holder, level, itemInUse);
                }
            });
        }
    }
}
