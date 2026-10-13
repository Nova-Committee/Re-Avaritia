package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.api.iface.IUndamageable;
import committee.nova.mods.avaritia.api.iface.InitEnchantItem;
import committee.nova.mods.avaritia.init.registry.ModArmorMaterial;
import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class NeutronHorseArmorItem extends Item implements InitEnchantItem, IUndamageable {
    public NeutronHorseArmorItem() {
        super(ModItems.applyId(new Item.Properties()
                .horseArmor(ModArmorMaterial.infinity_horse_armor)
                .stacksTo(1)
                .rarity(ModRarities.RARE)
                .fireResistant()));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(Enchantments.FROST_WALKER)) {
            return 10;
        } else if (enchantmentHolder.is(Enchantments.PROTECTION)) {
            return 10;
        } else if (enchantmentHolder.is(Enchantments.FEATHER_FALLING)) {
            return 10;
        }
        return 0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NonNull TooltipDisplay display,
                                @NotNull Consumer<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        var registries = context.registries();
        if (registries != null) {
            var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
            tooltipComponents.accept(ModTooltips.INIT_ENCHANT.args(Enchantment.getFullname(enchantments.getOrThrow(Enchantments.FROST_WALKER), 10)).build());
            tooltipComponents.accept(ModTooltips.INIT_ENCHANT.args(Enchantment.getFullname(enchantments.getOrThrow(Enchantments.PROTECTION), 10)).build());
            tooltipComponents.accept(ModTooltips.INIT_ENCHANT.args(Enchantment.getFullname(enchantments.getOrThrow(Enchantments.FEATHER_FALLING), 10)).build());
        }
        super.appendHoverText(stack, context, display, tooltipComponents, isAdvanced);
    }
}
