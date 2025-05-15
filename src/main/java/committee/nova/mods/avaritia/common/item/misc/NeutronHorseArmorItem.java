package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.iface.ITooltip;
import committee.nova.mods.avaritia.api.iface.InitEnchantItem;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModArmorMaterial;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * InfinityHorseArmorItem
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/4/5 20:04
 */
public class NeutronHorseArmorItem extends AnimalArmorItem implements ITooltip, InitEnchantItem {
    private static final String TEX_PATH = "textures/item/armor/horse/neutron_horse_armor.png";

    public NeutronHorseArmorItem() {
        super(ModArmorMaterial.infinite_armor, BodyType.EQUESTRIAN, false,
                new Item.Properties()
                        .stacksTo(1)
                        .rarity(ModRarities.RARE)
                        .fireResistant()
                        .setNoRepair());
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(@NotNull ItemStack stack) {
        return 10;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean hasCustomEntity(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @Nullable Entity createEntity(@NotNull Level level, Entity location, @NotNull ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public int getInitEnchantLevel(ItemStack stack, Enchantment enchantment) {
        Holder<Enchantment> sweeping_edge =
                player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(enchantment);
        if (enchantment == Enchantments.FROST_WALKER) return 10;
        else if (enchantment == Enchantments.ALL_DAMAGE_PROTECTION) return 10;
        else if (enchantment == Enchantments.FALL_PROTECTION) return 10;
        else return 0;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(ModTooltips.INIT_ENCHANT.args(Enchantments.FROST_WALKER.getFullname(10)).build());
        tooltipComponents.add(ModTooltips.INIT_ENCHANT.args(Enchantments.ALL_DAMAGE_PROTECTION.getFullname(10)).build());
        tooltipComponents.add(ModTooltips.INIT_ENCHANT.args(Enchantments.FALL_PROTECTION.getFullname(10)).build());
        appendTooltip(stack, context, tooltipComponents, isAdvanced, "neutron_horse_armor");
    }
}
