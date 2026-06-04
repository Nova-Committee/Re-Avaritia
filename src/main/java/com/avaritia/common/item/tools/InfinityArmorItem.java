package com.avaritia.common.item.tools;

import com.avaritia.Const;

import com.avaritia.api.iface.item.IUndamageable;
import com.avaritia.api.utils.lang.TextUtils;
import com.avaritia.common.entity.ImmortalItemEntity;
import com.avaritia.init.registry.ModArmorMaterial;
import com.avaritia.init.registry.ModEntityTypes;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModRarities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/21 15:12
 * Version: 1.0
 */
public class InfinityArmorItem extends Item implements IUndamageable {
    private final ArmorType armorType;

    public InfinityArmorItem(ArmorType pSlot, Item.Properties properties) {
        super(ModItems.applyId(properties)
                .humanoidArmor(ModArmorMaterial.infinity_armor, pSlot)
                .rarity(ModRarities.COSMIC.getValue())
                .fireResistant()
                .stacksTo(1)
        );
        this.armorType = pSlot;
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
    public boolean isGazeDisguise(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity target) {
        return true;
    }

    @Override
    public boolean makesPiglinsNeutral(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> adder, @NotNull TooltipFlag isAdvanced) {
        if (armorType.getSlot() == EquipmentSlot.HEAD) {
            adder.accept(Component.literal(""));
            adder.accept(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("effect.minecraft.night_vision")));
        }
        if (armorType.getSlot() == EquipmentSlot.CHEST) {
            adder.accept(Component.literal(""));
            adder.accept(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("attribute.name.generic.flying_speed")));
        }
        if (armorType.getSlot() == EquipmentSlot.LEGS) {
            adder.accept(Component.literal(""));
            adder.accept(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("attribute.name.generic.walking_speed")));
        }
        if (armorType.getSlot() == EquipmentSlot.FEET) {
            adder.accept(Component.literal(""));
            adder.accept(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtils.makeSANIC(I18n.get("tooltip.infinity")) + ChatFormatting.RESET + ChatFormatting.BLUE + "% ").append(I18n.get("attribute.name.generic.movement_speed")));
        }
        super.appendHoverText(stack, context, display, adder, isAdvanced);
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntityTypes.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

//    @Override
//    public @Nullable ResourceLocation getArmorTexture(@NotNull ItemStack stack, @NotNull Entity entity, @NotNull EquipmentSlot slot, ArmorMaterial.@NotNull Layer layer, boolean innerModel) {
//        return Const.rl("textures/models/armor/infinity_armor.png");
//    }
}
