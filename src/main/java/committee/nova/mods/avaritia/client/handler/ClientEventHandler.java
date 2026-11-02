package committee.nova.mods.avaritia.client.handler;

import committee.nova.mods.avaritia.api.util.lang.TextUtils;
import committee.nova.mods.avaritia.common.item.tools.InfinityArmorItem;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySwordItem;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinityTridentItem;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.util.ToolUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FogType;

import java.util.List;

/**
 * 26.3 Fabric 侧没有对应的 Neo 事件（ExtractBlockScreenEffectEvent / ViewportEvent.RenderFog /
 * ItemTooltipEvent），相关逻辑收敛为静态方法，由保留的客户端 mixin（屏幕遮罩抑制、雾距、tooltip 替换）调用。
 */
public final class ClientEventHandler {
    private ClientEventHandler() {
    }

    /** 玩家拥有无尽能力时抑制（火/方块内/水中）全屏遮罩；由屏幕遮罩渲染处 mixin 调用。 */
    public static boolean shouldSuppressScreenEffect() {
        Player player = Minecraft.getInstance().player;
        return player != null && ToolUtils.isInfinite(player);
    }

    /** 无尽头盔视野：熔岩/细雪中是否扩展雾平面；由 ClientFogMixin 调用。 */
    public static boolean isInfiniteFogView(Camera camera) {
        Entity entity = camera.entity();
        if (!(entity instanceof Player player)) return false;
        if (!ToolUtils.isWearingInfinityHelmet(player)) return false;
        FogType fogType = camera.getFluidInCamera();
        return fogType == FogType.LAVA || fogType == FogType.POWDER_SNOW;
    }

    public static float infinityFogNear() {
        return -8.0F;
    }

    public static float infinityFogFar() {
        return Math.min(96.0F, Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0F);
    }

    public static void applyTooltipLines(List<Component> tooltip, ItemStack stack) {
        if (ModConfig.isSwordAttackEndless.get()
                && (stack.getItem() instanceof InfinitySwordItem
                || stack.getItem() instanceof InfinityTridentItem)) {
            Component infinityDamage = Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity")))
                    .append(" ")
                    .append(Component.translatable("tooltip.infinity.desc").withStyle(ChatFormatting.DARK_GREEN));
            replaceOrAppendTooltipLine(tooltip,
                    I18n.get(Attributes.ATTACK_DAMAGE.value().getDescriptionId()), infinityDamage);
        } else if (stack.getItem() instanceof InfinityArmorItem) {
            replaceFirstTooltipLine(tooltip, I18n.get(Attributes.ARMOR.value().getDescriptionId()),
                    Component.literal("+").withStyle(ChatFormatting.BLUE)
                            .append(Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity"))))
                            .append(" ")
                            .append(Component.translatable("tooltip.armor.desc").withStyle(ChatFormatting.BLUE)));
            replaceFirstTooltipLine(tooltip, I18n.get(Attributes.ARMOR_TOUGHNESS.value().getDescriptionId()),
                    Component.literal("+").withStyle(ChatFormatting.BLUE)
                            .append(Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity"))))
                            .append(" ")
                            .append(Component.translatable("tooltip.armor_toughness.desc").withStyle(ChatFormatting.BLUE)));
        }
    }

    private static void replaceOrAppendTooltipLine(List<Component> tooltip, String marker, Component replacement) {
        if (!replaceFirstTooltipLine(tooltip, marker, replacement)) {
            // 新版属性 tooltip 可能不再生成攻击伤害行，找不到时主动补一行，保证无尽剑仍显示无限伤害。
            tooltip.add(replacement);
        }
    }

    private static boolean replaceFirstTooltipLine(List<Component> tooltip, String marker, Component replacement) {
        for (int x = 0; x < tooltip.size(); x++) {
            if (tooltip.get(x).getString().contains(marker)) {
                tooltip.set(x, replacement);
                return true;
            }
        }
        return false;
    }
}
