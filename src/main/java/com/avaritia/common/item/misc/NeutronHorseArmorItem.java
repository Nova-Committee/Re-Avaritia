package com.avaritia.common.item.misc;

import com.avaritia.api.common.enchant.InitEnchantment;
import com.avaritia.api.iface.item.InitEnchantItem;
import com.avaritia.common.entity.ImmortalItemEntity;
import com.avaritia.init.registry.ModEnchants;
import com.avaritia.init.registry.ModEntities;
import com.avaritia.init.registry.ModRarities;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.avaritia.init.registry.ModArmorMaterial.infinity_horse_armor;

public class NeutronHorseArmorItem extends Item implements InitEnchantItem {
    private final InitEnchantment FROST_WALKER = new InitEnchantment(ModEnchants.FROST_WALKER, 10);
    private final InitEnchantment ALL_DAMAGE_PROTECTION = new InitEnchantment(Enchantments.PROTECTION, 10);
    private final InitEnchantment FALL_PROTECTION = new InitEnchantment(Enchantments.FEATHER_FALLING, 4);
    public NeutronHorseArmorItem() {
        super(infinity_horse_armor,
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
    public int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder) {
        if (enchantmentHolder.is(ModEnchants.FROST_WALKER)) {
            return 10;
        }else if (enchantmentHolder.is(Enchantments.PROTECTION)) {
            return 10;
        }else if (enchantmentHolder.is(Enchantments.FEATHER_FALLING)) {
            return 4;
        }
        return 0;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents,
                                @NotNull TooltipFlag isAdvanced) {
        this.FROST_WALKER.appendHoverText(context, tooltipComponents);
        this.ALL_DAMAGE_PROTECTION.appendHoverText(context, tooltipComponents);
        this.FALL_PROTECTION.appendHoverText(context, tooltipComponents);
    }
}
