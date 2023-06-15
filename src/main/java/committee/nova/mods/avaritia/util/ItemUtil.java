package committee.nova.mods.avaritia.util;

import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 17:02
 * Version: 1.0
 */
public class ItemUtil {

    public static ItemStack mapEquals(ItemStack stack, Map<ItemStack, Integer> map) {
        for (ItemStack itemStack : map.keySet()) {
            if (itemStack.getItem() == stack.getItem()) {
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }
}
