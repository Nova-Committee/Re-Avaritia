package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class DimensionClientGameModeMixin {
    @Inject(method = "performUseItemOn", at = @At("HEAD"), cancellable = true)
    private void avaritia$ringBeforeContainer(LocalPlayer player, InteractionHand hand, BlockHitResult hit,
                                            CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof NeutronRingItem ring && !player.isSpectator()) {
            cir.setReturnValue(ring.onItemUseFirst(stack, new UseOnContext(player, hand, hit)));
        }
    }
}
