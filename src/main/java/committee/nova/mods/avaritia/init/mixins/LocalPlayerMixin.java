package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    // Only movement/sprint checks change; item-use state and the blocking animation remain intact.
    @Redirect(method = {"aiStep", "canStartSprinting"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean avaritia$shieldDoesNotSlowMovement(LocalPlayer player) {
        return player.isUsingItem() && !(player.getUseItem().getItem() instanceof InfinityShieldItem);
    }
}
