package com.avaritia.common.item.tools.blaze;

import com.avaritia.api.common.enchant.InitEnchantment;
import com.avaritia.api.iface.ITooltip;
import com.avaritia.api.iface.item.ISwitchable;
import com.avaritia.api.iface.item.InitEnchantItem;
import com.avaritia.init.registry.ModDataComponents;
import com.avaritia.init.registry.ModItems;
import com.avaritia.init.registry.ModRarities;
import com.avaritia.init.registry.ModToolTiers;
import com.avaritia.init.registry.modes.ToolMode;
import com.avaritia.util.ToolUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:00
 * Version: 1.0
 */
public class BlazeAxeItem extends AxeItem implements ITooltip, ISwitchable, InitEnchantItem {
    private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.FIRE_ASPECT, 10);

    public BlazeAxeItem() {
        super(ModToolTiers.BLAZE, 0, ModToolTiers.BLAZE.speed(),
                new Properties()
                        .component(ModDataComponents.TOOL_MODE, ToolMode.DEFAULT)
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
        );
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder) {
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
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        if (player.isCrouching() && !world.isClientSide()) {
            switchMode(world, player, hand, "smelt");
            return InteractionResult.SUCCESS;
        }
        return super.use(world, player, hand);
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miningEntity) {
        if (!level.isClientSide() && isActive(stack, "smelt") && miningEntity instanceof Player player) {
            ToolUtils.melting(state, level, pos, player, stack);
            if (state.is(BlockTags.LOGS)) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.refined_coal.get())));
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
        return super.mineBlock(stack, level, state, pos, miningEntity);
    }
}
