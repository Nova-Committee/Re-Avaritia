package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.utils.lang.TextUtils;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModArmorMaterial;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/21 15:12
 * Version: 1.0
 */
public class InfinityArmorItem extends ArmorItem {
    public InfinityArmorItem(Type pSlot) {
        super(
                ModArmorMaterial.infinite_armor,
                pSlot,
                new Properties()
                        .rarity(ModRarities.COSMIC)
                        .fireResistant()
                        .stacksTo(1)
        );
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnderMask(@NotNull ItemStack stack, @NotNull Player player, @NotNull EnderMan enderMan) {
        return true;
    }

    @Override
    public boolean makesPiglinsNeutral(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        if (type.getSlot() == EquipmentSlot.HEAD) {
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("effect.minecraft.night_vision")));
        }
        if (type.getSlot() == EquipmentSlot.CHEST) {
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("attribute.name.generic.flying_speed")));
        }
        if (type.getSlot() == EquipmentSlot.LEGS) {
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("attribute.name.generic.walking_speed")));
        }
        if (type.getSlot() == EquipmentSlot.FEET) {
            tooltipComponents.add(Component.literal(""));
            tooltipComponents.add(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("attribute.name.generic.movement_speed")));
        }
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public @Nullable ResourceLocation getArmorTexture(@NotNull ItemStack stack, @NotNull Entity entity, @NotNull EquipmentSlot slot, ArmorMaterial.@NotNull Layer layer, boolean innerModel) {
        return Static.rl("textures/models/infinity_armor.png");
    }
}
