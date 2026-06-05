package com.avaritia.client.handler;

import com.avaritia.Const;
import com.avaritia.api.utils.lang.TextUtils;
import com.avaritia.common.item.tools.InfinityArmorItem;
import com.avaritia.common.item.tools.infinity.InfinitySwordItem;
import com.avaritia.init.config.ModConfig;
import com.avaritia.util.ToolUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

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
        if (ModConfig.isSwordAttackEndless.get() && event.getItemStack().getItem() instanceof InfinitySwordItem swordItem) {
            for (int x = 0; x < event.getToolTip().size(); x++) {
                if (event.getToolTip().get(x).getString().contains(I18n.get("attribute.name.generic.attack_damage"))) {
                    var endlessDamage = ModConfig.isSwordAttackEndless.get();
                    event.getToolTip().set(x, Component.literal(endlessDamage ? TextUtils.makeFabulous(I18n.get("tooltip.infinity")) : String.valueOf(swordItem.getTier().attackDamageBonus())).append(" ").append(Component.translatable("tooltip.infinity.desc").withStyle(ChatFormatting.DARK_GREEN)));
                    return;
                }
            }
        } else if (event.getItemStack().getItem() instanceof InfinityArmorItem) {
            for (int x = 0; x < event.getToolTip().size(); x++) {
                if (event.getToolTip().get(x).getString().contains(I18n.get("attribute.name.generic.armor"))) {
                    event.getToolTip().set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.armor.desc").withStyle(ChatFormatting.BLUE)));
                    return;
                } else if (event.getToolTip().get(x).getString().contains(I18n.get("attribute.name.generic.armor_toughness"))) {
                    event.getToolTip().set(x, Component.literal("+").withStyle(ChatFormatting.BLUE).append(Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.infinity")))).append(" ").append(Component.translatable("tooltip.armor_toughness.desc").withStyle(ChatFormatting.BLUE)));
                    return;
                }
            }
        }
    }
}
