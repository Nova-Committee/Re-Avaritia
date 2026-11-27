package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.common.item.tools.InfinityArmorItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public abstract class GameplayPiglinAiMixin {
    @Inject(method = "isWearingGold", at = @At("HEAD"), cancellable = true)
    private static void avaritia$neutralArmor(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        for (ItemStack armor : entity.getArmorSlots()) {
            if (armor.getItem() instanceof InfinityArmorItem) { cir.setReturnValue(true); return; }
        }
    }
}
