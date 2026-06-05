package com.avaritia.common.item.tools.blaze;

import com.avaritia.init.registry.ModItems;

import com.avaritia.api.common.enchant.InitEnchantment;
import com.avaritia.api.iface.ITooltip;
import com.avaritia.api.iface.item.ISwitchable;
import com.avaritia.api.iface.item.InitEnchantItem;
import com.avaritia.init.registry.ModDataComponents;
import com.avaritia.init.registry.ModRarities;
import com.avaritia.init.registry.ModToolTiers;
import com.avaritia.init.registry.modes.ToolMode;
import com.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:00
 * Version: 1.0
 */
public class BlazePickaxeItem extends Item implements ITooltip, ISwitchable, InitEnchantItem {
    private final InitEnchantment fire_aspect;

    public BlazePickaxeItem() {
        super(ModItems.properties()
                        .component(ModDataComponents.TOOL_MODE, ToolMode.DEFAULT)
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
                        .pickaxe(ModToolTiers.BLAZE, 0, ModToolTiers.BLAZE.speed())
        );

        this.fire_aspect = new InitEnchantment(Enchantments.FIRE_ASPECT, 10);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.FIRE_ASPECT)) {
            return 10;
        }
        return 0;
    }


    @Override
    public boolean hasDescTooltip() {
        return true;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display, @NotNull Consumer<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.fire_aspect.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            switchMode(world, player, hand, "smelt");
            return InteractionResult.SUCCESS;
        }
        return super.use(world, player, hand);
    }

        @Override
        public boolean mineBlock (@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState
        state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity){
            if (level instanceof ServerLevel serverLevel && isActive(stack, "smelt") && miningEntity instanceof Player player) {
                ToolUtils.melting(state, serverLevel, pos, player, stack);
            }
            return super.mineBlock(stack, level, state, pos, miningEntity);
        }

}
