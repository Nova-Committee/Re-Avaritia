package com.avaritia.common.item.tools.crystal;

import com.avaritia.api.common.enchant.InitEnchantment;
import com.avaritia.api.iface.ITooltip;
import com.avaritia.api.iface.item.InitEnchantItem;
import com.avaritia.api.utils.ItemUtils;
import com.avaritia.init.registry.ModRarities;
import com.avaritia.init.registry.ModToolTiers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:25
 * Version: 1.0
 */
public class CrystalPickaxeItem extends Item implements ITooltip, InitEnchantItem {

private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.FORTUNE, 3);

private final String name;

    public CrystalPickaxeItem(String name) {
        super(new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
                        .pickaxe(ModToolTiers.CRYSTAL, 0, ModToolTiers.BLAZE.speed())
        );
        this.name = name;
    }

    @Override
    public boolean hasDescTooltip() {
        return true;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public int getEnchantmentLevel(@NonNull ItemInstance stack, @NonNull Holder<Enchantment> enchantment) {
        return 0;
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        return 100F;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Holder<Enchantment> SILK_TOUCH =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SILK_TOUCH);
        if (player.isShiftKeyDown()) {
            if (EnchantmentHelper.getTagEnchantmentLevel(SILK_TOUCH, stack) > 0) {
                ItemUtils.clearEnchants(stack,SILK_TOUCH);
                if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer)
                    serverPlayer.sendSystemMessage(Component.translatable("tooltip.crystal_pickaxe.enchant_1"), true);
            } else {
                stack.enchant(SILK_TOUCH, 1);
                if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer)
                    serverPlayer.sendSystemMessage(Component.translatable("tooltip.crystal_pickaxe.enchant_2"), true);
            }
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }
        return super.use(world, player, hand);
    }

    @Override
    public int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.FORTUNE)) {
            return 3;
        }
        return 0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }
}
