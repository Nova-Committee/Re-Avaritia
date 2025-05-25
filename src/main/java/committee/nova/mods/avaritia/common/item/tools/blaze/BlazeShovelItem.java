package committee.nova.mods.avaritia.common.item.tools.blaze;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.common.item.iface.IItemEnchant;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:00
 * Version: 1.0
 */
public class BlazeShovelItem extends ShovelItem implements ITooltip, IItemEnchant {
    private final String name;
    private final InitEnchantment initEnchantment;

    public BlazeShovelItem(String name) {
        super(ModToolTiers.BLAZE,
                new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
                        .attributes(createAttributes(ModToolTiers.BLAZE, 0, ModToolTiers.BLAZE.getSpeed()))
        );

        this.name = name;
        this.initEnchantment = new InitEnchantment(Enchantments.FIRE_ASPECT, 10);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return this.initEnchantment.getLevel(enchantment);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        this.appendTooltip(stack, context, tooltipComponents, isAdvanced, name);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        var level = pContext.getLevel();
        var blockpos = pContext.getClickedPos();
        var blockstate = level.getBlockState(blockpos);
        if (blockstate.is(Blocks.GRAVEL)) {
            level.setBlockAndUpdate(blockpos, Blocks.NETHERRACK.defaultBlockState());
            return InteractionResult.SUCCESS;
        } else if (blockstate.is(Blocks.SAND)) {
            level.setBlockAndUpdate(blockpos, Blocks.SOUL_SAND.defaultBlockState());
            return InteractionResult.SUCCESS;
        } else if (blockstate.is(Blocks.DIRT)) {
            level.setBlockAndUpdate(blockpos, Blocks.SOUL_SOIL.defaultBlockState());
            return InteractionResult.SUCCESS;
        } else return super.useOn(pContext);
    }
}
