package committee.nova.mods.avaritia.common.item.tools.blaze;

import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.InitEnchantItem;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 20:00
 * Version: 1.0
 */
public class BlazeHoeItem extends Item implements ITooltip, ISwitchable, InitEnchantItem {

    public BlazeHoeItem() {
        super(ModItems.properties()
                .rarity(ModRarities.EPIC)
                .stacksTo(1)
                .fireResistant()
                .hoe(ModToolTiers.BLAZE, 0, ModToolTiers.BLAZE.speed())
        );
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder.is(Enchantments.FIRE_ASPECT) ? 10 : 0;
    }

    @Override
    public boolean hasDescTooltip() {
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display,
                                @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        var registries = context.registries();
        if (registries != null) {
            Holder<Enchantment> fireAspect = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FIRE_ASPECT);
            tooltipComponents.accept(ModTooltips.INIT_ENCHANT.args(Enchantment.getFullname(fireAspect, 10)).build());
        }
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            switchMode(world, player, hand, "smelt");
            return InteractionResult.SUCCESS;
        }
        return super.use(world, player, hand);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        var level = pContext.getLevel();
        var stack = pContext.getItemInHand();
        var blockpos = pContext.getClickedPos();
        var blockstate = level.getBlockState(blockpos);
        var player = pContext.getPlayer();
        if (isActive(stack, "smelt")) {
            if (blockstate.is(Blocks.SOUL_SAND)) {
                level.setBlockAndUpdate(blockpos, Blocks.SOUL_SOIL.defaultBlockState());
                level.playSound(player, blockpos, SoundEvents.HOE_TILL.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            } else if (blockstate.is(Blocks.SOUL_SOIL)) {
                level.setBlockAndUpdate(blockpos, ModBlocks.soul_farmland.get().defaultBlockState());
                level.playSound(player, blockpos, SoundEvents.HOE_TILL.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            } else if (blockstate.is(ModBlocks.soul_farmland.get())) {
                return InteractionResult.PASS;
            }
        } else if (!isActive(stack, "smelt")) {
            if (blockstate.is(ModBlocks.soul_farmland.get())) {
                return InteractionResult.PASS;
            }
        }
        return super.useOn(pContext);
    }
}
