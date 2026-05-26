package com.avaritia.api.common.enchant;

import com.avaritia.init.registry.ModTooltips;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/17 13:31
 * @Description:
 */
public class InitEnchantment {
    private final ResourceKey<Enchantment> enchantment;
    private final int level;

    public InitEnchantment(ResourceKey<Enchantment> enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    public void appendHoverText(Item.TooltipContext context, List<Component> tooltipComponents) {
        HolderLookup.Provider registries = context.registries();
        if (registries != null) {
            HolderLookup.RegistryLookup<Enchantment> registrylookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
            registrylookup.get(this.enchantment).ifPresent((holder) -> tooltipComponents.add(ModTooltips.INIT_ENCHANT.args(Enchantment.getFullname(holder, this.level)).build()));
        }
    }

    public int getLevel(Holder<Enchantment> enchantment) {
        return enchantment.is(this.enchantment) ? this.level : 0;
    }
}
