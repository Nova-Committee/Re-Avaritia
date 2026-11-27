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
    // 必须注入 performUseItemOn 而非外层 useItemOn：useItemOn 通过 startPrediction 包裹执行并负责
    // 发送 ServerboundUseItemOnPacket，在其 HEAD 取消会导致服务端永远收不到右键交互（戒指无法选中）。
    // performUseItemOn 仍在 startPrediction 回调内执行，取消它既保留发包又让戒指优先于方块/useOn。
    @Inject(method = "performUseItemOn", at = @At("HEAD"), cancellable = true)
    private void avaritia$ringBeforeContainer(LocalPlayer player, InteractionHand hand, BlockHitResult hit,
                                            CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof NeutronRingItem ring && !player.isSpectator()) {
            cir.setReturnValue(ring.onItemUseFirst(stack, new UseOnContext(player, hand, hit)));
        }
    }
}
