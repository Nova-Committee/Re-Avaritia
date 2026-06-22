package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Legacy client-side container kept for old infinity chest network classes.
 * The current infinity chest uses real menu slots backed by InfinityChestTile.
 */
public class InfinityChestContainer extends SimpleContainer {
    public static final int SIZE = 15 * 9;
    public static final int WIDTH = 15;
    public static final int HEIGHT = 9;

    public final ArrayList<String> sortedObject = new ArrayList<>();
    public final ArrayList<String> viewingObject = new ArrayList<>();
    public final ArrayList<String> formatCount = new ArrayList<>();
    public ArrayList<String> sortedItems = new ArrayList<>();
    private double scrollTo = 0.0D;

    public InfinityChestContainer(InfinityChestMenu menu) {
        super(SIZE);
    }

    public void onChangeItems() {
        sortedObject.clear();
        sortedObject.addAll(sortedItems);
        scrollOffset(0);
    }

    public void onScrollTo(double scrollTo) {
        this.scrollTo = scrollTo;
        scrollOffset(0);
    }

    public double getScrollOn() {
        return scrollTo;
    }

    public void scrollOffset(int offset) {
        viewingObject.clear();
        viewingObject.addAll(sortedObject.subList(0, Math.min(sortedObject.size(), SIZE)));
        updateDummySlots(true);
    }

    public double onMouseScrolled(boolean isUp) {
        return scrollTo;
    }

    public void refreshContainer(boolean fullUpdate) {
        updateDummySlots(fullUpdate);
    }

    public void updateDummySlots(boolean fullUpdate) {
        formatCount.clear();
        for (int slot = 0; slot < SIZE; slot++) {
            this.setItem(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public @NotNull ItemStack getItem(int index) {
        return index >= 0 && index < SIZE ? super.getItem(index) : ItemStack.EMPTY;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }
}
