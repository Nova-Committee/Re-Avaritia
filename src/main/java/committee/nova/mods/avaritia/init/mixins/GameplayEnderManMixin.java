package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.tools.InfinityArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enderman.class)
public abstract class GameplayEnderManMixin {
    // 26.3: the disguise gate moved to isBeingStaredBy (PLAYER_NOT_WEARING_DISGUISE_ITEM predicate).
    @Inject(method = "isBeingStaredBy", at = @At("HEAD"), cancellable = true)
    private void avaritia$helmetMask(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof InfinityArmorItem) cir.setReturnValue(false);
    }
}
