package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class RegistryRarityNamesMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void avaritia$displayColor(CallbackInfoReturnable<Component> cir) {
        ChatFormatting color = ModRarities.nameColor((ItemStack) (Object) this);
        if (color != null) cir.setReturnValue(cir.getReturnValue().copy().withStyle(color));
    }

    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void avaritia$tooltipColor(Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
        ChatFormatting color = ModRarities.nameColor((ItemStack) (Object) this);
        if (color != null && !cir.getReturnValue().isEmpty()) {
            List<Component> lines = cir.getReturnValue();
            lines.set(0, lines.get(0).copy().withStyle(color));
        }
    }
}
