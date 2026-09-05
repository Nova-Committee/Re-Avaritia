package committee.nova.mods.avaritia.init.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    // Backport the shield's 26.1.2 UseEffects: full movement speed and sprinting while raised.
    // Only movement checks change; actual use state and blocking animation remain intact.
    @ModifyExpressionValue(method = {"aiStep", "canStartSprinting"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean avaritia$shieldDoesNotSlowMovement(boolean usingItem) {
        return usingItem && !(((LocalPlayer) (Object) this).getUseItem().getItem() instanceof InfinityShieldItem);
    }
}
