package committee.nova.mods.avaritia.init.mixins;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityShieldItem;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityShieldMixin {
    // 26.1.2 UseEffects.interactVibrations=false: retain use state, suppress only its two vibrations.
    @WrapWithCondition(method = {"startUsingItem", "stopUsingItem"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/core/Holder;)V"))
    private boolean avaritia$shieldUseIsSilent(LivingEntity entity, Holder<GameEvent> event) {
        return !(entity.getUseItem().getItem() instanceof InfinityShieldItem);
    }
}
