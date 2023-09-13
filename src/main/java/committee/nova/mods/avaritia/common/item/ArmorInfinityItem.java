package committee.nova.mods.avaritia.common.item;

import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModArmorMaterial;
import committee.nova.mods.avaritia.init.registry.ModCreativeModeTabs;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.util.lang.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/21 15:12
 * Version: 1.0
 */
public class ArmorInfinityItem extends ArmorItem {
    public ArmorInfinityItem(Type pSlot) {
        super(
                ModArmorMaterial.infinite_armor,
                pSlot,
                new Properties()
                        .fireResistant()
                        .stacksTo(1)
        );
    }


    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return true;
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return true;
    }


    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        return ModItems.COSMIC_RARITY;
    }


    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        if (type.getSlot() == EquipmentSlot.HEAD) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtil.makeSANIC("SANIC") + ChatFormatting.RESET + ChatFormatting.BLUE + "% NIGHT VISION"));
        }
        if (type.getSlot() == EquipmentSlot.CHEST) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtil.makeSANIC("SANIC") + ChatFormatting.RESET + ChatFormatting.BLUE + "% FLY SPEED"));
        }
        if (type.getSlot() == EquipmentSlot.LEGS) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtil.makeSANIC("SANIC") + ChatFormatting.RESET + ChatFormatting.BLUE + "% WALK SPEED"));
        }
        if (type.getSlot() == EquipmentSlot.FEET) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal(ChatFormatting.BLUE + "+" + ChatFormatting.ITALIC + TextUtil.makeSANIC("SANIC") + ChatFormatting.RESET + ChatFormatting.BLUE + "% SPEED"));
        }
        super.appendHoverText(pStack, pLevel, tooltip, pIsAdvanced);
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }


}
