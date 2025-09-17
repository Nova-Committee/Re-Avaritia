package committee.nova.mods.avaritia.api.util.game;

import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.api.iface.ISortContainer;
import committee.nova.mods.avaritia.api.util.math.SortingType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/3 11:27
 * @Description:
 */
public class MainNoNullList extends NonNullList<ItemStack> {
    private final ISortContainer sortContainer;
    private final ItemStack initialElement;
    public final List<ItemStack> delegate;
    public static MainNoNullList of(List<ItemStack> list, ItemStack defaultValue, ISortContainer playerInventory) {
        Validate.notNull(defaultValue);
        return new MainNoNullList(list, defaultValue, playerInventory);
    }

    public static MainNoNullList ofSize(int size, ItemStack defaultValue, ISortContainer playerInventory) {
        Validate.notNull(defaultValue);
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i=0; i<size; ++i)
            list.add(defaultValue);
        return new MainNoNullList(list, defaultValue, playerInventory);
    }

    protected MainNoNullList(List<ItemStack> delegate, ItemStack initialElement, ISortContainer sortContainer) {
        super(delegate, initialElement);
        this.initialElement = initialElement;
        this.delegate = delegate;
        this.sortContainer = sortContainer;
    }

    @Override
    public @NotNull NonNullList<ItemStack> subList(int fromIndex, int toIndex) {
        return new MainNoNullList(this.delegate.subList(fromIndex, toIndex), this.initialElement, this.sortContainer);
    }

    @Override
    public @NotNull ItemStack set(int index, @NotNull ItemStack itemStack) {
        if (index > 8) {
            if (this.sortContainer.getSortingType() != SortingType.NONE)
                this.sortContainer.needToSort();
            this.sortContainer.needToUpdateInfinitorySize();
        }
        return super.set(index, itemStack);
    }

    @Override
    public void add(int index, @NotNull ItemStack itemStack) {
        if (index > 8) {
            if (this.sortContainer.getSortingType() != SortingType.NONE)
                this.sortContainer.needToSort();
            this.sortContainer.needToUpdateInfinitorySize();
        }
        super.add(index, itemStack);
    }
}
