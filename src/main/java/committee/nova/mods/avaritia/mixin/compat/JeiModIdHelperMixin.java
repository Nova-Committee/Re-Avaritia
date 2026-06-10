package committee.nova.mods.avaritia.mixin.compat;

import committee.nova.mods.avaritia.compat.jei.JeiTooltipDeduplication;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(targets = "mezz.jei.library.helpers.ModIdHelper", remap = false)
public abstract class JeiModIdHelperMixin {
    @Inject(method = "getModNameForTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private <T> void avaritia$hideDuplicateJadeItemModName(ITypedIngredient<T> typedIngredient,
                                                           CallbackInfoReturnable<Optional<Component>> cir) {
        if (JeiTooltipDeduplication.shouldSuppressItemModNameTooltip(typedIngredient)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
