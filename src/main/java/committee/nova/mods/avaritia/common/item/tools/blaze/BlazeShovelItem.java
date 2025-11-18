package committee.nova.mods.avaritia.common.item.tools.blaze;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import committee.nova.mods.avaritia.util.FuncUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:00
 * Version: 1.0
 */
public class BlazeShovelItem extends ShovelItem implements ITooltip, ISwitchable, InitEnchantItem {
    public static Map<Block, Block> TRANS_MAP = Map.ofEntries(
            Map.entry(Blocks.DIRT, Blocks.SOUL_SOIL),
            Map.entry(Blocks.SAND, Blocks.SOUL_SAND),
            Map.entry(Blocks.STONE, Blocks.BLACKSTONE),
            Map.entry(Blocks.DEEPSLATE, Blocks.BASALT),
            Map.entry(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN),
            Map.entry(Blocks.GRASS_BLOCK, Blocks.WARPED_NYLIUM),
            Map.entry(Blocks.PODZOL, Blocks.CRIMSON_NYLIUM),
            Map.entry(Blocks.RED_MUSHROOM, Blocks.CRIMSON_FUNGUS),
            Map.entry(Blocks.BROWN_MUSHROOM, Blocks.WARPED_FUNGUS),
            Map.entry(Blocks.PACKED_ICE, Blocks.MAGMA_BLOCK),
            Map.entry(Blocks.BEACON, Blocks.ANCIENT_DEBRIS),
            Map.entry(Blocks.STONE_BRICKS, Blocks.NETHER_BRICKS),
            Map.entry(Blocks.BRICKS, Blocks.RED_NETHER_BRICKS),
            Map.entry(Blocks.TORCH, Blocks.SOUL_TORCH),
            Map.entry(Blocks.LANTERN, Blocks.SOUL_LANTERN),
            Map.entry(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE),
            Map.entry(Blocks.DEAD_BUSH, Blocks.WITHER_ROSE)
    );
    private final InitEnchantment initEnchantment;

    public BlazeShovelItem() {
        super(ModToolTiers.BLAZE,
                new Properties()
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
                        .attributes(createAttributes(ModToolTiers.BLAZE, 0, ModToolTiers.BLAZE.getSpeed()))
        );

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
    public boolean hasDescTooltip() {
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown()) {
            switchMode(level, player, usedHand, "blaze_shovel_trans");
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        var player = pContext.getPlayer();
        var stack = pContext.getItemInHand();
        var level = pContext.getLevel();
        var blockpos = pContext.getClickedPos();
        if (isActive(stack, "blaze_shovel_trans")) {
            return FuncUtils.transBlock(level, player, blockpos, TRANS_MAP);
        } else return super.useOn(pContext);
    }
}
