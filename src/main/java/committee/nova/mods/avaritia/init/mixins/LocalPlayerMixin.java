package committee.nova.mods.avaritia.init.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    // Compose with other mods' movement/sprint checks without changing actual item-use state.
    @ModifyExpressionValue(method = {"aiStep", "canStartSprinting"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean avaritia$shieldDoesNotSlowMovement(boolean usingItem) {
        return usingItem && !(((LocalPlayer) (Object) this).getUseItem().getItem() instanceof InfinityShieldItem);
    }
}
