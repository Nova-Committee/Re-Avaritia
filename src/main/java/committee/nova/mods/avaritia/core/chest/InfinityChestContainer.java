package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import committee.nova.mods.avaritia.util.SortUtils;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author cnlimiter
 */
public class InfinityChestContainer extends SimpleContainer {
    public static final int SIZE = 15 * 9;
    public static final int WIDTH = 15;
    public static final int HEIGHT = 9;
    public final ArrayList<ItemSuper> sortedObject = new ArrayList<>();
    public final ArrayList<ItemSuper> viewingObject = new ArrayList<>();
    public final ArrayList<String> formatCount = new ArrayList<>();
    public final InfinityChestMenu menu;
    public ArrayList<ItemSuper> sortedItems = new ArrayList<>();
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
        if (sortedObject.size() <= SIZE) {
            viewingObject.clear();
            viewingObject.addAll(sortedObject);
        } else {
            int i = (int) Math.ceil((double) sortedObject.size() / WIDTH);
            i -= HEIGHT;
            int j = Math.round(i * (float) scrollTo);
            if (offset != 0) {
                j += offset;
                j = Math.max(0, Math.min(i, j));
                scrollTo = (double) j / (double) i;
            }
            viewingObject.clear();
            viewingObject.addAll(sortedObject.subList(j * WIDTH, Math.min(sortedObject.size(), j * WIDTH + SIZE)));
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
                ArrayList<ItemSuper> temp = new ArrayList<>();
                char head = this.menu.filter.charAt(0);
                if (head == '*') {
                    String s = this.menu.filter.substring(1);
                    for (var itemSuper : sortedItems) if (itemSuper.toString().contains(s)) temp.add(itemSuper);
                } else if (head == '$') {
                    String s = this.menu.filter.substring(1);
                    for (var itemSuper : sortedItems) {
                        ArrayList<String> tags = new ArrayList<>();
                        itemSuper.getStack().getTags().forEach(itemTagKey -> tags.add(itemTagKey.location().getPath()));
                        for (String tag : tags) {
                            if (tag.contains(s)) {
                                temp.add(itemSuper);
                                break;
                            }
                        }
                    }
                } else {
                    for (var itemSuper : sortedItems) {
                        if (itemSuper.toString().contains(this.menu.filter)) temp.add(itemSuper);
                        else {
                            if (itemSuper.getStack().getDisplayName().getString().toLowerCase().contains(this.menu.filter))
                                temp.add(itemSuper);
                        }
                    }
                }
                sortedItems = temp;
            }
            switch (this.menu.sortType) {
                case SortUtils.Sort.ID_ASCENDING -> {
                    sortedItems.sort((s1, s2) -> SortUtils.sortFromRightID(s1.toString(), s2.toString()));
                }
                case SortUtils.Sort.ID_DESCENDING -> {
                    sortedItems.sort(Collections.reverseOrder((s1, s2) -> SortUtils.sortFromRightID(s1.toString(), s2.toString())));
                }
                case SortUtils.Sort.NAMESPACE_ID_ASCENDING -> {
                    sortedItems.sort(Comparator.comparing(ItemSuper::toString));
                }
                case SortUtils.Sort.NAMESPACE_ID_DESCENDING -> {
                    sortedItems.sort(Collections.reverseOrder(Comparator.comparing(ItemSuper::toString)));
                }
                case SortUtils.Sort.MIRROR_ID_ASCENDING -> {
                    sortedItems.sort((s1, s2) -> SortUtils.sortFromMirrorID(s1.toString(), s2.toString()));
                }
                case SortUtils.Sort.MIRROR_ID_DESCENDING -> {
                    sortedItems.sort(Collections.reverseOrder((s1, s2) -> SortUtils.sortFromMirrorID(s1.toString(), s2.toString())));
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
                var id = viewingObject.get(j);

                //叠堆数为1避开原版的数字渲染
                if (fullUpdate) {
                    // 使用chest.getStackInSlot获取正确的ItemStack（包括NBT）
                    // chest.getStackInSlot的slot参数是IItemHandler的slot
                    // 这里j + 27对应实际的slot
                    if (j < this.menu.chest.getSlots()) {
                        ItemStack stack = this.menu.chest.getStackInSlot(j + 27);
                        this.setItem(j, stack);
                    } else {
                        // 如果超出范围，使用基础物品
                        ItemStack stack = id.getStack();
                        this.setItem(j, stack);
                    }
                }

                long count;
                if (this.menu.chest.storageItems.containsKey(id)) {
                    count = this.menu.chest.storageItems.get(id);
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
    public @NotNull ItemStack getItem(int index) {
        if (index < 0 || index >= viewingObject.size()) return ItemStack.EMPTY;
        var itemId = viewingObject.get(index);
        if (itemId == null || itemId.getStack().isEmpty()) return ItemStack.EMPTY;

        long count = this.menu.chest.storageItems.getOrDefault(itemId, 0L);
        return itemId.getStack().copyWithCount((int) Math.min(Integer.MAX_VALUE, count));
    }

    @Override
    public void setChanged() {
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }
}
