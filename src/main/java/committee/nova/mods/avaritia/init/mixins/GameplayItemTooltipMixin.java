package committee.nova.mods.avaritia.init.mixins;

import committee.nova.mods.avaritia.api.util.lang.TextUtils;
import committee.nova.mods.avaritia.common.item.tools.InfinityArmorItem;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySwordItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
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
public abstract class GameplayItemTooltipMixin {
    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void avaritia$infinityAttributes(Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        boolean sword = stack.getItem() instanceof InfinitySwordItem && ModConfig.isSwordAttackEndless.get();
        boolean armor = stack.getItem() instanceof InfinityArmorItem;
        if (!sword && !armor) return;
        List<Component> lines = cir.getReturnValue();
        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i).getString();
            if (sword && text.contains(I18n.get("attribute.name.generic.attack_damage"))) {
                lines.set(i, Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity"))).append(" ")
                        .append(Component.translatable("tooltip.infinity.desc").withStyle(ChatFormatting.DARK_GREEN)));
            } else if (armor && text.contains(I18n.get("attribute.name.generic.armor_toughness"))) {
                lines.set(i, Component.literal("+" + TextUtils.makeFabulous(I18n.get("tooltip.infinity")))
                        .append(" ").append(Component.translatable("tooltip.armor_toughness.desc")).withStyle(ChatFormatting.BLUE));
            } else if (armor && text.contains(I18n.get("attribute.name.generic.armor"))) {
                lines.set(i, Component.literal("+" + TextUtils.makeFabulous(I18n.get("tooltip.infinity")))
                        .append(" ").append(Component.translatable("tooltip.armor.desc")).withStyle(ChatFormatting.BLUE));
            }
        }
    }
}
