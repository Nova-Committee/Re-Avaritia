package committee.nova.mods.avaritia.common.wrappers;

import committee.nova.mods.avaritia.Const;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: cnlimiter
 */
public class InfiniteItemHandler implements IItemHandler, IItemHandlerModifiable {
    // 每页的槽位数
    public static final int SLOTS_PER_PAGE = 54;
    // 最大页数（近似无限）
    public static final int MAX_PAGES = 1000000;
    // 最大物品数量（可根据需要调整）
    public static final int MAX_ITEMS = 1000000;

    // 存储所有物品的列表
    private final List<ItemStack> items;
    // 当前滚动位置
    private int scrollPosition = 0;
    // 搜索关键词
    private String searchQuery = "";
    // 分类方式
    private SortType sortType = SortType.NONE;
    // 按模组分类的物品
    private Map<String, List<ItemStack>> modItemsMap;
    // 是否需要重建模组物品映射
    private boolean rebuildModMap = true;

    public enum SortType {
        NONE, NAME, COUNT, MOD, CATEGORY
    }

    public InfiniteItemHandler() {
        this.items = new ArrayList<>();
        initializeSlots();
    }

    private void initializeSlots() {
        // 确保至少有一页的槽位
        while (items.size() < SLOTS_PER_PAGE) {
            items.add(ItemStack.EMPTY);
        }
    }

    @Override
    public int getSlots() {
        // 返回可见的槽位数
        return SLOTS_PER_PAGE;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        // 计算实际槽位索引
        int actualSlot = scrollPosition + slot;
        if (actualSlot < 0 || actualSlot >= items.size()) {
            return ItemStack.EMPTY;
        }
        return items.get(actualSlot);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 检查是否超过最大物品数量
        if (getNonEmptyItemCount() >= MAX_ITEMS) {
            return stack;
        }

        // 尝试与现有物品合并
        for (int i = 0; i < items.size(); i++) {
            ItemStack existing = items.get(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, stack)) {
                int spaceLeft = existing.getMaxStackSize() - existing.getCount();
                if (spaceLeft > 0) {
                    int amountToInsert = Math.min(stack.getCount(), spaceLeft);
                    if (!simulate) {
                        existing.grow(amountToInsert);
                        rebuildModMap = true;
                    }
                    return stack.copyWithCount(stack.getCount() - amountToInsert);
                }
            }
        }

        // 找到空槽位插入
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                int amountToInsert = Math.min(stack.getCount(), stack.getMaxStackSize());
                if (!simulate) {
                    items.set(i, stack.copyWithCount(amountToInsert));
                    rebuildModMap = true;
                }
                return stack.copyWithCount(stack.getCount() - amountToInsert);
            }
        }

        // 如果没有空槽位，添加新槽位
        if (items.size() < MAX_ITEMS) {
            int amountToInsert = Math.min(stack.getCount(), stack.getMaxStackSize());
            if (!simulate) {
                items.add(stack.copyWithCount(amountToInsert));
                rebuildModMap = true;
            }
            return stack.copyWithCount(stack.getCount() - amountToInsert);
        }

        return stack;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        int actualSlot = scrollPosition + slot;
        if (actualSlot < 0 || actualSlot >= items.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = items.get(actualSlot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int amountToExtract = Math.min(amount, existing.getCount());
        ItemStack extracted = existing.copyWithCount(amountToExtract);

        if (!simulate) {
            existing.shrink(amountToExtract);
            if (existing.isEmpty()) {
                items.set(actualSlot, ItemStack.EMPTY);
            }
            rebuildModMap = true;
        }

        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        int actualSlot = scrollPosition + slot;
        while (actualSlot >= items.size()) {
            items.add(ItemStack.EMPTY);
        }
        items.set(actualSlot, stack);
        rebuildModMap = true;
    }

    // 滚动位置控制
    public int getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(int position) {
        this.scrollPosition = Math.max(0, Math.min(position, Math.max(0, items.size() - SLOTS_PER_PAGE)));
    }

    public int getMaxScrollPosition() {
        return Math.max(0, items.size() - SLOTS_PER_PAGE);
    }

    // 获取非空物品数量
    public int getNonEmptyItemCount() {
        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    // 搜索功能
    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase();
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    // 获取过滤后的物品列表（用于搜索）
    public List<ItemStack> getFilteredItems() {
        if (searchQuery.isEmpty()) {
            return new ArrayList<>(items);
        }

        List<ItemStack> filtered = new ArrayList<>();
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.getDisplayName().getString().toLowerCase().contains(searchQuery)) {
                filtered.add(stack);
            }
        }
        return filtered;
    }

    // 分类功能
    public void setSortType(SortType type) {
        this.sortType = type;
        if (type != SortType.NONE) {
            sortItems();
        }
    }

    public SortType getSortType() {
        return sortType;
    }

    // 排序物品
    private void sortItems() {
        List<ItemStack> nonEmptyItems = new ArrayList<>();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                nonEmptyItems.add(stack);
            }
        }

        // 根据排序类型排序
        nonEmptyItems.sort((stack1, stack2) -> {
            switch (sortType) {
                case NAME:
                    return stack1.getDisplayName().getString().compareTo(stack2.getDisplayName().getString());
                case COUNT:
                    return Integer.compare(stack2.getCount(), stack1.getCount());
                case MOD:
                    String modId1 = Const.getItemName(stack1.getItem()).getNamespace();
                    String modId2 = Const.getItemName(stack2.getItem()).getNamespace();
                    return modId1.compareTo(modId2);
                case CATEGORY:
                    // 这里可以根据物品类别进行排序
                    return stack1.getItem().getDescription().getString().compareTo(stack2.getItem().getDescription().getString());
                default:
                    return 0;
            }
        });

        // 清空列表并重新添加排序后的物品
        items.clear();
        items.addAll(nonEmptyItems);

        // 确保至少有一页的槽位
        initializeSlots();

        rebuildModMap = true;
    }

    // 按模组分类获取物品
    public Map<String, List<ItemStack>> getItemsByMod() {
        if (rebuildModMap || modItemsMap == null) {
            rebuildModItemsMap();
        }
        return modItemsMap;
    }

    private void rebuildModItemsMap() {
        modItemsMap = new HashMap<>();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                String modId = Const.getItemName(stack.getItem()).getNamespace();
                modItemsMap.computeIfAbsent(modId, k -> new ArrayList<>()).add(stack);
            }
        }
        rebuildModMap = false;
    }

    // NBT 数据持久化
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag itemsList = new ListTag();

        for (ItemStack stack : items) {
            CompoundTag itemTag = new CompoundTag();
            if (!stack.isEmpty()) {
                stack.save(itemTag);
            }
            itemsList.add(itemTag);
        }

        tag.put("Items", itemsList);
        tag.putInt("ScrollPosition", scrollPosition);
        tag.putString("SearchQuery", searchQuery);
        tag.putString("SortType", sortType.name());
        return tag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        items.clear();
        ListTag itemsList = nbt.getList("Items", 10);

        for (int i = 0; i < itemsList.size(); i++) {
            CompoundTag itemTag = itemsList.getCompound(i);
            if (itemTag.isEmpty()) {
                items.add(ItemStack.EMPTY);
            } else {
                items.add(ItemStack.of(itemTag));
            }
        }

        scrollPosition = nbt.getInt("ScrollPosition");
        searchQuery = nbt.getString("SearchQuery");
        sortType = SortType.valueOf(nbt.getString("SortType"));
        rebuildModMap = true;
    }

    // 获取所有物品
    public List<ItemStack> getAllItems() {
        List<ItemStack> allItems = new ArrayList<>();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                allItems.add(stack);
            }
        }
        return allItems;
    }
}
