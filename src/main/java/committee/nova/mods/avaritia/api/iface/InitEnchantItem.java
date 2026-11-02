package committee.nova.mods.avaritia.api.iface;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/11/15 14:42
 * @Description: 自带附魔
 */
public interface InitEnchantItem {
    int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder);
}