package committee.nova.mods.avaritia.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/6 上午12:50
 * @Description:
 */
public class MenuUtils {
    public static ItemStack findInInventory(Player player, Inventory playerInventory, Item item) {
        if (player.getMainHandItem().getItem() == item) {
            return player.getMainHandItem();
        }
        else if (player.getOffhandItem().getItem() == item) {
            return player.getOffhandItem();
        }
        else {
            for (int x = 0; x < playerInventory.getContainerSize(); x++) {
                ItemStack stack = playerInventory.getItem(x);
                if (stack.getItem() == item) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
