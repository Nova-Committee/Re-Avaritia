package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.util.SortUtils;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author cnlimiter
 */
public class InfinityChestContainer extends SimpleContainer {
    public static final int SIZE = 15 * 9;
    public final ArrayList<ItemStack> sortedObject = new ArrayList<>();
    public final ArrayList<ItemStack> viewingObject = new ArrayList<>();
    public final ArrayList<String> formatCount = new ArrayList<>();
    private final InfinityChestMenu menu;
    protected ArrayList<ItemStack> sortedItems = new ArrayList<>();
    private double scrollTo = 0.0D;

    public InfinityChestContainer(InfinityChestMenu menu) {
        super(SIZE);
        this.menu = menu;
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
        if (sortedObject.size() <= SIZE) {//显示多少格子
            viewingObject.clear();
            viewingObject.addAll(sortedObject);
        } else {
            int i = (int) Math.ceil(sortedObject.size() / 15.0D);
            i -= 9;
            int j = Math.round(i * (float) scrollTo);
            if (offset != 0) {
                j += offset;
                j = Math.max(0, Math.min(i, j));
                scrollTo = (double) j / (double) i;
            }
            viewingObject.clear();
            viewingObject.addAll(sortedObject.subList(j * 15, Math.min(sortedObject.size(), j * 15 + SIZE)));
        }
        updateDummySlots(true);
    }

    public double onMouseScrolled(boolean isUp) {
        if (isUp) scrollOffset(-1);
        else scrollOffset(1);
        return scrollTo;
    }

    public void refreshContainer(boolean fullUpdate) {
        if (!this.menu.level.isClientSide) return;
        if ((fullUpdate || this.menu.sortType >= 6) && !this.menu.LShifting) {
            sortedItems = new ArrayList<>(this.menu.chest.storageItems.keySet());
            if (!this.menu.filter.isEmpty()) {
                ArrayList<ItemStack> temp = new ArrayList<>();
                char head = this.menu.filter.charAt(0);
                if (head == '*') {
                    String s = this.menu.filter.substring(1);
                    for (var itemStack : sortedItems) if (StorageUtils.getItemId(itemStack).contains(s)) temp.add(itemStack);
                } else if (head == '$') {
                    String s = this.menu.filter.substring(1);
                    for (var itemStack : sortedItems) {
                        ArrayList<String> tags = new ArrayList<>();
                        itemStack.getTags().forEach(itemTagKey -> tags.add(itemTagKey.location().getPath()));
                        for (String tag : tags) {
                            if (tag.contains(s)) {
                                temp.add(itemStack);
                                break;
                            }
                        }
                    }
                } else {
                    for (var itemStack : sortedItems) {
                        if (StorageUtils.getItemId(itemStack).contains(this.menu.filter)) temp.add(itemStack);
                        else {
                            if (itemStack.getDisplayName().getString().toLowerCase().contains(this.menu.filter))
                                temp.add(itemStack);
                        }
                    }
                }
                sortedItems = temp;
            }
            switch (this.menu.sortType) {
                case SortUtils.Sort.ID_ASCENDING -> {
                    sortedItems.sort(SortUtils::sortFromRightID);
                }
                case SortUtils.Sort.ID_DESCENDING -> {
                    sortedItems.sort(Collections.reverseOrder(SortUtils::sortFromRightID));
                }
                case SortUtils.Sort.NAMESPACE_ID_ASCENDING -> {
                    sortedItems.sort(Comparator.comparing(StorageUtils::getItemId));
                }
                case SortUtils.Sort.NAMESPACE_ID_DESCENDING -> {
                    sortedItems.sort(Collections.reverseOrder(Comparator.comparing(StorageUtils::getItemId)));
                }
                case SortUtils.Sort.MIRROR_ID_ASCENDING -> {
                    sortedItems.sort(SortUtils::sortFromMirrorID);
                }
                case SortUtils.Sort.MIRROR_ID_DESCENDING -> {
                    sortedItems.sort(Collections.reverseOrder(SortUtils::sortFromMirrorID));
                }
                case SortUtils.Sort.COUNT_ASCENDING -> {
                    sortedItems.sort((s1, s2) -> SortUtils.sortFromCount(s1, s2, this.menu.chest.storageItems, false));

                }
                case SortUtils.Sort.COUNT_DESCENDING -> {
                    sortedItems.sort((s1, s2) -> SortUtils.sortFromCount(s1, s2, this.menu.chest.storageItems, true));

                }
            }
            onChangeItems();
            return;
        }
        updateDummySlots(fullUpdate);
    }

    public void updateDummySlots(boolean fullUpdate) {
        formatCount.clear();
        for (int j = 0; j < SIZE; j++) {
            if (j < viewingObject.size() && viewingObject.get(j) != null) {
                var itemStack = viewingObject.get(j);

                //叠堆数为1避开原版的数字渲染
                if (fullUpdate) this.setItem(j, itemStack);
                long count;
                if (this.menu.chest.storageItems.containsKey(itemStack)) {
                    count = this.menu.chest.storageItems.get(itemStack);
                } else {
                    formatCount.add(j, "§c0");
                    continue;
                }
                if (count < 1000L) formatCount.add(j, String.valueOf(count));
                else if (count < Long.MAX_VALUE) {
                    String stringCount = StorageUtils.DECIMAL_FORMAT.format(count);
                    stringCount = stringCount.substring(0, 4);
                    if (stringCount.endsWith(",")) stringCount = stringCount.substring(0, 3);
                    stringCount = stringCount.replace(",", ".");
                    if (count < 1000000L) stringCount += "K";
                    else if (count < 1000000000L) stringCount += "M";
                    else if (count < 1000000000000L) stringCount += "G";
                    else if (count < 1000000000000000L) stringCount += "T";
                    else if (count < 1000000000000000000L) stringCount += "P";
                    else stringCount += "E";
                    formatCount.add(j, stringCount);
                    // 9,223,372,036,854,775,807L
                    // e  p   t   g   m   k
                } else formatCount.add(j, "MAX");

            } else this.setItem(j, ItemStack.EMPTY);
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }
}
