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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author: cnlimiter
 */
public class InfiniteItemHandler implements IItemHandler, IItemHandlerModifiable {
    // 每页的槽位数
    public static final int SLOTS_PER_PAGE = 54;
    // 最大页数（近乎无限）
    public static final int MAX_PAGES = 1000000;

    // 存储所有物品的列表
    private final List<ItemStack> items;
    // 当前页码
    private int currentPage;
    // 搜索关键词
    private String searchQuery = "";
    // 分类方式
    private SortType sortType = SortType.NONE;
    // 是否自动整理
    private boolean autoOrganize = false;

    public enum SortType {
        NONE, NAME, COUNT, MOD
    }

    public InfiniteItemHandler() {
        this.items = new ArrayList<>();
        this.currentPage = 0;
        // 初始化第一页的槽位
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
        // 返回当前页的槽位数
        return SLOTS_PER_PAGE;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        // 计算实际槽位索引
        int actualSlot = currentPage * SLOTS_PER_PAGE + slot;
        if (actualSlot < 0 || actualSlot >= items.size()) {
            return ItemStack.EMPTY;
        }
        return items.get(actualSlot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int actualSlot = currentPage * SLOTS_PER_PAGE + slot;

        // 如果槽位超出当前范围，扩展列表
        while (actualSlot >= items.size()) {
            items.add(ItemStack.EMPTY);
        }

        ItemStack existing = items.get(actualSlot);

        if (existing.isEmpty()) {
            int amountToInsert = Math.min(stack.getCount(), stack.getMaxStackSize());
            if (!simulate) {
                items.set(actualSlot, new ItemStack(stack.getItem(), amountToInsert));
            }
            return stack.copyWithCount(stack.getCount() - amountToInsert);
        } else if (ItemStack.isSameItemSameTags(existing, stack)) {
            int spaceLeft = existing.getMaxStackSize() - existing.getCount();
            int amountToInsert = Math.min(stack.getCount(), spaceLeft);
            if (!simulate) {
                existing.grow(amountToInsert);
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

        int actualSlot = currentPage * SLOTS_PER_PAGE + slot;
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
                if (autoOrganize) {
                    CompletableFuture.runAsync(this::organizeItems);
                }
            }
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
        int actualSlot = currentPage * SLOTS_PER_PAGE + slot;
        while (actualSlot >= items.size()) {
            items.add(ItemStack.EMPTY);
        }
        items.set(actualSlot, stack);
        if (autoOrganize) {
            CompletableFuture.runAsync(this::organizeItems);
        }
    }

    // 分页相关方法
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        if (page >= 0 && page < MAX_PAGES) {
            this.currentPage = page;
            // 确保当前页有足够的槽位
            while ((currentPage + 1) * SLOTS_PER_PAGE > items.size()) {
                items.add(ItemStack.EMPTY);
            }
            debugPrintCurrentPage();
        }
    }

    public int getTotalPages() {
        return (items.size() + SLOTS_PER_PAGE - 1) / SLOTS_PER_PAGE;
    }

    // 搜索功能
    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase();
    }

    public String getSearchQuery() {
        return searchQuery;
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

    // 自动整理开关
    public void setAutoOrganize(boolean enabled) {
        this.autoOrganize = enabled;
        if (enabled) {
            CompletableFuture.runAsync(this::organizeItems);
        }
    }

    public boolean isAutoOrganize() {
        return autoOrganize;
    }

    // 获取过滤后的物品列表（用于搜索）
    public List<ItemStack> getFilteredItems() {
        if (searchQuery.isEmpty()) {
            return new ArrayList<>(items);
        }

        return items.stream()
                .filter(stack -> !stack.isEmpty())
                .filter(stack -> stack.getDisplayName().getString().toLowerCase().contains(searchQuery))
                .collect(Collectors.toList());
    }

    // 排序物品
    private void sortItems() {
        List<ItemStack> nonEmptyItems = items.stream()
                .filter(stack -> !stack.isEmpty())
                .sorted((stack1, stack2) -> {
                    switch (sortType) {
                        case NAME:
                            return stack1.getDisplayName().getString().compareTo(stack2.getDisplayName().getString());
                        case COUNT:
                            return Integer.compare(stack2.getCount(), stack1.getCount());
                        case MOD:
                            String modId1 = Const.getItemName(stack1.getItem()).getNamespace();
                            String modId2 = Const.getItemName(stack2.getItem()).getNamespace();
                            return modId1.compareTo(modId2);
                        default:
                            return 0;
                    }
                })
                .toList();

        // 清空列表并重新添加排序后的物品
        items.clear();
        items.addAll(nonEmptyItems);

        // 确保至少有一页的槽位
        initializeSlots();
    }

    // 自动整理物品
    public void organizeItems() {
        // 按物品类型分组
        Map<ItemStack, Integer> itemCounts = new HashMap<>();

        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                boolean found = false;
                for (Map.Entry<ItemStack, Integer> entry : itemCounts.entrySet()) {
                    if (ItemStack.isSameItemSameTags(entry.getKey(), stack)) {
                        itemCounts.put(entry.getKey(), entry.getValue() + stack.getCount());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    itemCounts.put(stack.copy(), stack.getCount());
                }
            }
        }

        // 清空列表
        items.clear();

        // 重新添加整理后的物品
        for (Map.Entry<ItemStack, Integer> entry : itemCounts.entrySet()) {
            ItemStack stack = entry.getKey();
            int totalCount = entry.getValue();

            while (totalCount > 0) {
                int stackSize = Math.min(totalCount, stack.getMaxStackSize());
                ItemStack newStack = stack.copy();
                newStack.setCount(stackSize);
                items.add(newStack);
                totalCount -= stackSize;
            }
        }

        // 确保至少有一页的槽位
        initializeSlots();
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
        tag.putInt("CurrentPage", currentPage);
        tag.putString("SearchQuery", searchQuery);
        tag.putString("SortType", sortType.name());
        tag.putBoolean("AutoOrganize", autoOrganize);
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

        currentPage = nbt.getInt("CurrentPage");
        searchQuery = nbt.getString("SearchQuery");
        sortType = SortType.valueOf(nbt.getString("SortType"));
        autoOrganize = nbt.getBoolean("AutoOrganize");
    }

    // 获取所有物品（用于 AE2 兼容）
    public List<ItemStack> getAllItems() {
        List<ItemStack> allItems = new ArrayList<>();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                allItems.add(stack);
            }
        }
        return allItems;
    }

    // 按模组分类获取物品
    public Map<String, List<ItemStack>> getItemsByMod() {
        Map<String, List<ItemStack>> modItems = new HashMap<>();

        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                String modId = Const.getItemName(stack.getItem()).getNamespace();
                modItems.computeIfAbsent(modId, k -> new ArrayList<>()).add(stack);
            }
        }

        return modItems;
    }

    // 在InfiniteItemHandler类中添加调试方法
    public void debugPrintCurrentPage() {
        System.out.println("当前页: " + currentPage);
        System.out.println("总页数: " + getTotalPages());
        System.out.println("当前页物品:");
        for (int i = 0; i < SLOTS_PER_PAGE; i++) {
            int actualSlot = currentPage * SLOTS_PER_PAGE + i;
            if (actualSlot < items.size() && !items.get(actualSlot).isEmpty()) {
                System.out.println("槽位 " + i + ": " + items.get(actualSlot).getDisplayName().getString() + " x" + items.get(actualSlot).getCount());
            }
        }
    }
}
