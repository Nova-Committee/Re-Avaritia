package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.iface.ITooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author: cnlimiter
 */
@Mixin(Item.class)
public abstract class ItemMixin implements ITooltip {

    @Override
    public boolean hasDescTooltip() {
        return false;
    }

    @Inject(
            method = "appendHoverText",
            at = @At(value = "TAIL")
    )
    public void avaritia$appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents,
                                         TooltipFlag isAdvanced, CallbackInfo ci) {
        if (this.hasDescTooltip()) {
            tooltipComponents.add(
                    Component.translatable("tooltip."
                            + ForgeRegistries.ITEMS.getKey((Item) (Object) this).toString().replace(":", ".")
                            + ".desc").withStyle(ChatFormatting.DARK_GRAY,  ChatFormatting.ITALIC)
            );
        }
    }
}
