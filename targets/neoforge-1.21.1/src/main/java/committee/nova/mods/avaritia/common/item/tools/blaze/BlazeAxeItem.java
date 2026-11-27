package committee.nova.mods.avaritia.common.item.tools.blaze;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.item.ISwitchable;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import committee.nova.mods.avaritia.init.registry.modes.ToolMode;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 20:00
 * Version: 1.0
 */
public class BlazeAxeItem extends AxeItem implements ITooltip, ISwitchable, InitEnchantItem {
    private final InitEnchantment initEnchantment = new InitEnchantment(Enchantments.FIRE_ASPECT, 10);

    public BlazeAxeItem() {
        super(ModToolTiers.BLAZE,
                new Properties()
                        .component(ModDataComponents.TOOL_MODE, ToolMode.DEFAULT)
                        .rarity(ModRarities.EPIC)
                        .stacksTo(1)
                        .fireResistant()
                        .attributes(createAttributes(ModToolTiers.BLAZE, 0, ModToolTiers.BLAZE.getSpeed()))
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
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.initEnchantment.appendHoverText(context, tooltipComponents);
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && !world.isClientSide) {
            switchMode(world, player, hand, "smelt");
            return InteractionResultHolder.success(stack);
        }
        return super.use(world, player, hand);
    }

}
