package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class DimensionServerGameModeMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void avaritia$ringBeforeContainer(ServerPlayer player, Level level, ItemStack stack,
                                            InteractionHand hand, BlockHitResult hit,
                                            CallbackInfoReturnable<InteractionResult> cir) {
        if (stack.getItem() instanceof NeutronRingItem ring && !player.isSpectator()) {
            if (!level.mayInteract(player, hit.getBlockPos())
                    || !player.mayUseItemAt(hit.getBlockPos(), hit.getDirection(), stack)) {
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }
            cir.setReturnValue(ring.onItemUseFirst(stack, new UseOnContext(player, hand, hit)));
        }
    }
}
