package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.Res;
import committee.nova.mods.avaritia.api.iface.InitEnchantItem;
import committee.nova.mods.avaritia.common.entity.ImmortalItemEntity;
import committee.nova.mods.avaritia.init.registry.ModEntities;
import committee.nova.mods.avaritia.init.registry.ModRarities;
import committee.nova.mods.avaritia.init.registry.ModTooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NeutronHorseArmorItem extends HorseArmorItem implements InitEnchantItem {
    public NeutronHorseArmorItem() {
        super(Integer.MAX_VALUE, new ResourceLocation(Const.MOD_ID, Res.NEUTRON_HORSE_ARMOR.getPath()),
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
    public int getEnchantmentValue(ItemStack stack) {
        return 10;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public @Nullable Entity createEntity(Level level, Entity location, ItemStack stack) {
        return ImmortalItemEntity.create(ModEntities.IMMORTAL.get(), level, location, stack);
    }

    @Override
    public int getInitEnchantLevel(ItemStack stack, Enchantment enchantment) {
        if (enchantment == Enchantments.FROST_WALKER) return 10;
        else if (enchantment == Enchantments.ALL_DAMAGE_PROTECTION) return 10;
        else if (enchantment == Enchantments.FALL_PROTECTION) return 10;
        else return 0;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(ModTooltips.INIT_ENCHANT.args(Enchantments.FROST_WALKER.getFullname(10)).build());
        tooltipComponents.add(ModTooltips.INIT_ENCHANT.args(Enchantments.ALL_DAMAGE_PROTECTION.getFullname(10)).build());
        tooltipComponents.add(ModTooltips.INIT_ENCHANT.args(Enchantments.FALL_PROTECTION.getFullname(10)).build());
    }
}
