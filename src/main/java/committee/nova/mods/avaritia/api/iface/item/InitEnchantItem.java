package committee.nova.mods.avaritia.api.iface.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/15 14:42
 * @Description: 鑷甫闄勯瓟
 */
public interface InitEnchantItem {
    int getInitEnchantLevel(ItemInstance stack, Holder<Enchantment> enchantmentHolder);
}
