package committee.nova.mods.avaritia.common.item.tools.blaze;

import committee.nova.mods.avaritia.api.iface.ISwitchable;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.InitEnchantItem;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModToolTiers;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 20:00
 * Version: 1.0
 */
public class BlazePickaxeItem extends Item implements ITooltip, ISwitchable, InitEnchantItem {

    public BlazePickaxeItem() {
        super(ModItems.properties()
                .rarity(ModRarities.EPIC)
                .stacksTo(1)
                .fireResistant()
                .pickaxe(ModToolTiers.BLAZE, 0, ModToolTiers.BLAZE.speed()));
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
}
