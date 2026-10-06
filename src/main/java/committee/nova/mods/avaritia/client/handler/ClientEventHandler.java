package committee.nova.mods.avaritia.client.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.utils.lang.TextUtils;
import committee.nova.mods.avaritia.common.item.tools.InfinityArmorItem;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySwordItem;
import committee.nova.mods.avaritia.common.item.tools.infinity.InfinitySpearItem;
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
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public final class ClientEventHandler {
    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderBlockScreenEffectEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && ToolUtils.isInfinite(player)
                && (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE
                || event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.BLOCK
                || event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.WATER)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        Camera camera = event.getCamera();
        Entity entity = camera.entity();

        if (!(entity instanceof Player player)) return;
        if (!ToolUtils.isWearingInfinityHelmet(player)) return;

        FogType fogType = camera.getFluidInCamera();
        if (fogType == FogType.LAVA || fogType == FogType.POWDER_SNOW) {
            float farPlane = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0F;
            event.setNearPlaneDistance(-8.0f);
            event.setFarPlaneDistance(Math.min(96.0f, farPlane));
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (ModConfig.isSwordAttackEndless.get()
                && (event.getItemStack().getItem() instanceof InfinitySwordItem
                || event.getItemStack().getItem() instanceof InfinitySpearItem)) {
            Component infinityDamage = Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity")))
                    .append(" ")
                    .append(Component.translatable("tooltip.infinity.desc").withStyle(ChatFormatting.DARK_GREEN));
            replaceOrAppendTooltipLine(event.getToolTip(),
                    I18n.get(Attributes.ATTACK_DAMAGE.value().getDescriptionId()), infinityDamage);
        } else if (event.getItemStack().getItem() instanceof InfinityArmorItem) {
            replaceFirstTooltipLine(event.getToolTip(), I18n.get(Attributes.ARMOR.value().getDescriptionId()),
                    Component.literal("+").withStyle(ChatFormatting.BLUE)
                            .append(Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity"))))
                            .append(" ")
                            .append(Component.translatable("tooltip.armor.desc").withStyle(ChatFormatting.BLUE)));
            replaceFirstTooltipLine(event.getToolTip(), I18n.get(Attributes.ARMOR_TOUGHNESS.value().getDescriptionId()),
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
