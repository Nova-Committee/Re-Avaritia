package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.misc.InfinityElytraItem;
import committee.nova.mods.avaritia.util.InfinityElytraUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class GameplayLocalPlayerMixin {
    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack avaritia$equippedElytra(LocalPlayer player, EquipmentSlot slot) {
        ItemStack elytra = InfinityElytraUtils.getInfinityElytraStack(player);
        return slot == EquipmentSlot.CHEST && !elytra.isEmpty() ? elytra : player.getItemBySlot(slot);
    }
    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean avaritia$nativeElytra(ItemStack stack, Item item) {
        return stack.is(item) || item == Items.ELYTRA && stack.getItem() instanceof InfinityElytraItem;
    }
    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ElytraItem;isFlyEnabled(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean avaritia$elytraFlightEnabled(ItemStack stack) {
        return InfinityElytraUtils.isFlyEnabled(stack);
    }
}
