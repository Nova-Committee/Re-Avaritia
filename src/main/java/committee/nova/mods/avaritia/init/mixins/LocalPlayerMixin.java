package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.UseEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    // Only movement/sprint checks change; item-use state and the blocking animation remain intact.
    // 26.3: input slowdown moved to modifyInput (called from aiStep).
    @Redirect(method = "modifyInput",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean avaritia$shieldDoesNotSlowInput(LocalPlayer player) {
        return player.isUsingItem() && !(player.getUseItem().getItem() instanceof InfinityShieldItem);
    }

    // 26.3: aiStep and canStartSprinting gate on the private isSlowDueToUsingItem.
    @Redirect(method = {"aiStep", "canStartSprinting"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSlowDueToUsingItem()Z"))
    private boolean avaritia$shieldDoesNotSlowMovement(LocalPlayer player) {
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof InfinityShieldItem) {
            return false;
        }
        return player.isUsingItem()
                && !((UseEffects) player.getUseItem().getOrDefault(DataComponents.USE_EFFECTS, UseEffects.DEFAULT)).canSprint();
    }
}
