package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.util.InfinityElytraUtils;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 26.3 滑翔已数据驱动（DataComponents.GLIDER + EQUIPPABLE）；无尽鞘翅仅带 GLIDER 组件
 * 且可经饰品栏携带，vanilla canGlideUsing 无法放行，故在 aiStep 的起飞调用处补齐触发。
 */
@Mixin(LocalPlayer.class)
public abstract class GameplayLocalPlayerMixin {
    @Redirect(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;tryToStartFallFlying()Z")
    )
    private boolean avaritia$tryStartFallFlying(LocalPlayer player) {
        if (player.tryToStartFallFlying()) return true;
        if (player.isFallFlying() || player.isInLiquid() || InfinityElytraUtils.isUsingOtherFlightMode(player)) return false;
        if (InfinityElytraUtils.getInfinityElytraStack(player).isEmpty()) return false;
        player.startFallFlying();
        return true;
    }
}
