package committee.nova.mods.avaritia.mixin;

import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "blockUsingItem", at = @At("HEAD"), cancellable = true)
    private void onBlockUsingItem(ServerLevel level, LivingEntity entity, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        ItemStack useItem = player.getUseItem();

        if (useItem.getItem() instanceof InfinityShieldItem) {

            if (entity.getSecondsToDisableBlocking() > 0.0F) {
                ci.cancel();
            }
        }
    }
}
