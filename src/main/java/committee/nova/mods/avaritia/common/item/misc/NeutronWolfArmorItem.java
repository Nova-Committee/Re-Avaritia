package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.api.common.enchant.InitEnchantment;
import committee.nova.mods.avaritia.api.iface.item.InitEnchantItem;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static committee.nova.mods.avaritia.init.registry.ModArmorMaterial.neutron_wolf_armor;

/**
 * 中子战狼铠（1.21.1）。
 * <p>
 * 1.21.1 的原版狼铠就是 {@link AnimalArmorItem} 的 {@code CANINE} 变体，
 * 因此中子版本沿用同一实现，只替换材质、耐久与固定附魔。
 */
public class NeutronWolfArmorItem extends AnimalArmorItem implements InitEnchantItem {
    private final InitEnchantment ALL_DAMAGE_PROTECTION = new InitEnchantment(Enchantments.PROTECTION, 10);

    public NeutronWolfArmorItem() {
        super(neutron_wolf_armor, AnimalArmorItem.BodyType.CANINE, false,
                new Item.Properties()
                        .stacksTo(1)
                        .durability(7777)
                        .rarity(ModRarities.RARE)
                        .fireResistant()
                        .setNoRepair());
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(@NotNull ItemStack stack) {
        return 10;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public int getInitEnchantLevel(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.PROTECTION)) {
            return 10;
        }
        return 0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        this.ALL_DAMAGE_PROTECTION.appendHoverText(context, tooltipComponents);
    }
}
