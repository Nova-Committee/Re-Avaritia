package committee.nova.mods.avaritia.api.utils.math;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/3 01:03
 * @Description:
 */
public enum SortingType {
    NONE(null, null),
    NAME(Comparator.comparing(i -> i.getHoverName().getString()), (i1, i2) -> i2.getHoverName().getString().compareTo(i1.getHoverName().getString())),
    QUANTITY(Comparator.comparingInt(ItemStack::getCount).thenComparing(i -> i.getHoverName().getString()), (i1, i2) -> {
        int ret = Integer.compare(i2.getCount(), i1.getCount());
        if (ret == 0)
            ret = i1.getHoverName().getString().compareTo(i2.getHoverName().getString());
        return ret;
    }),
    ID(Comparator.comparingInt((ItemStack i) -> Item.getId(i.getItem())).thenComparing(i -> i.getHoverName().getString()), (i1, i2) -> {
        int ret = Integer.compare(Item.getId(i2.getItem()), Item.getId(i1.getItem()));
        if (ret == 0)
            ret = i1.getHoverName().getString().compareTo(i2.getHoverName().getString());
        return ret;
    });

    private Comparator<ItemStack> comparatorAscending;
    private Comparator<ItemStack> comparatorDescending;

    SortingType(Comparator<ItemStack> comparatorAscending, Comparator<ItemStack> comparatorDescending) {
        this.comparatorAscending = comparatorAscending;
        this.comparatorDescending = comparatorDescending;
    }

    public void sort(ArrayList<ItemStack> list, boolean ascending) {
        Comparator<ItemStack> comparator = ascending ? this.comparatorAscending : this.comparatorDescending;
        if (comparator != null)
            list.sort(comparator);
    }
    public SortingType getNextType() {
        int ordinal = this.ordinal()+1;
        if (ordinal >= SortingType.values().length)
            ordinal = 0;
        return SortingType.values()[ordinal];
    }
}
