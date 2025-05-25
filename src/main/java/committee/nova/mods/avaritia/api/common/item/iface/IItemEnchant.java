package committee.nova.mods.avaritia.api.common.item.iface;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/15 14:42
 * @Description: 自带附魔
 */
public interface IItemEnchant {
    int getInitEnchantLevel(ItemStack stack, Holder<Enchantment> enchantmentHolder);
}
