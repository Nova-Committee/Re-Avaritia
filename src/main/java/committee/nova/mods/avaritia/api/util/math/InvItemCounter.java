package committee.nova.mods.avaritia.api.util.math;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/28 12:13
 * @Description:
 */
public class InvItemCounter {
    private final HashMap<Item, Integer> invItemAmount = new HashMap<>();
    private final TreeSet<Integer> nbtItemIndex = new TreeSet<>();
    private final TreeSet<Integer> noNbtItemIndex = new TreeSet<>();
    private final NonNullList<ItemStack> itemList;
    private final Integer[] nbtItemIndexArray;
    private final Integer[] noNbtItemIndexArray;

    public InvItemCounter(Inventory inventory) {
        itemList = inventory.items;
        for (int i = 9; i < itemList.size(); i++) add(i);
        for (int i = 0; i < 9; i++) add(i);
        nbtItemIndexArray = nbtItemIndex.toArray(new Integer[]{});
        noNbtItemIndexArray = noNbtItemIndex.toArray(new Integer[]{});
    }

    private void add(int i) {
        ItemStack invItem = itemList.get(i);
        if (invItem.isEmpty()) return;
        if (invItem.hasTag()) nbtItemIndex.add(i);
        else {
            if (invItemAmount.containsKey(invItem.getItem()))
                invItemAmount.replace(invItem.getItem(), invItemAmount.get(invItem.getItem()) + invItem.getCount());
            else invItemAmount.put(invItem.getItem(), invItem.getCount());
            noNbtItemIndex.add(i);
        }
    }

    public int getCount(Item item) {
        return invItemAmount.getOrDefault(item, 0);
    }

    public int getCount(ItemStack itemStack) {
        int count = 0;
        if (itemStack.hasTag()) {
            for (Integer itemIndex : nbtItemIndex) {
                ItemStack itemStack1 = itemList.get(itemIndex);
                if (ItemStack.isSameItemSameTags(itemStack, itemStack1)) {
                    count += itemStack.getCount();
                }
            }
            return count;
        } else return getCount(itemStack.getItem());
    }

    public Integer[] getNbtItemIndex() {
        return nbtItemIndexArray;
    }

    public Integer[] getNoNbtItemIndex() {
        return noNbtItemIndexArray;
    }
}
