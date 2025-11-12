package committee.nova.mods.avaritia.core.chest;

import com.google.common.collect.Lists;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

/**
 * ChestHandler - 抽象物品存储处理器
 * <p>
 * 这个抽象类实现了IItemHandler接口，用于处理大型储物容器的物品存储逻辑。
 * 它支持超大量物品存储（使用long类型计数），并提供各种物品操作方法。
 * 
 * @author cnlimiter
 */
public abstract class ChestHandler implements IItemHandler {
    /**
     * 存储物品的映射表，键为物品堆栈，值为该物品的数量（使用long类型以支持超大数量）
     */
    public final HashMap<ItemStack, Long> storageItems = new HashMap<>();
    
    /**
     * 物品槽位临时列表，用于在GUI中显示物品
     */
    private List<ItemStack> slotItemTemp = Lists.newArrayList();

    /**
     * 构造函数
     */
    public ChestHandler() {}

    /**
     * 检查此处理器是否已被移除
     * 
     * @return 如果已被移除则返回true，否则返回false
     */
    public abstract boolean isRemoved();

    /**
     * 当物品发生变化时调用此方法
     * 
     * @param itemId 物品堆栈
     * @param listChanged 列表是否发生变化
     */
    public void onItemChanged(ItemStack itemId, boolean listChanged) {
        if (listChanged) updateItemKeys();
    }

    /**
     * 更新物品键列表，将当前存储的所有物品添加到临时列表中
     */
    public void updateItemKeys() {
        slotItemTemp.addAll(storageItems.keySet());
    }

    /**
     * 检查是否包含指定物品
     * 
     * @param item 要检查的物品堆栈
     * @return 如果包含该物品则返回true，否则返回false
     */
    public boolean hasItem(ItemStack item) {
        return storageItems.containsKey(item);
    }

    /**
     * 获取指定物品的数量（受限于Integer.MAX_VALUE）
     * 
     * @param item 物品堆栈
     * @return 物品数量，最大为Integer.MAX_VALUE
     */
    public int getItemAmount(ItemStack item) {
        return (int) Long.min(Integer.MAX_VALUE, storageItems.getOrDefault(item, 0L));
    }

    /**
     * 获取指定物品的实际数量（long类型，无上限限制）
     * 
     * @param item 物品堆栈
     * @return 物品实际数量
     */
    public long getRealItemAmount(ItemStack item) {
        return storageItems.getOrDefault(item, 0L);
    }

    /**
     * 获取指定物品的存储数量（受限于Integer.MAX_VALUE）
     * 
     * @param item 物品堆栈
     * @return 物品存储数量，最大为Integer.MAX_VALUE
     */
    public int getStorageAmount(ItemStack item) {
        return (int) Long.min(Integer.MAX_VALUE, storageItems.getOrDefault(item, 0L));
    }

    /**
     * 计算指定物品还能存储多少数量
     * 
     * @param itemStack 物品堆栈
     * @return 还能存储的数量，最大为Integer.MAX_VALUE
     */
    public int canStorageAmount(ItemStack itemStack) {
        long a = storageItems.getOrDefault(itemStack, 0L);
        if (a == 0L) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    /**
     * 检查是否还能存储指定物品
     * 
     * @param item 物品堆栈
     * @return 如果能存储则返回true，否则返回false
     */
    public boolean canStorageItem(ItemStack item) {
        if (storageItems.containsKey(item)) {
            return storageItems.get(item) < Long.MAX_VALUE;
        } else return true;
    }

    /**
     * 添加物品到存储中
     * 
     * @param itemStack 要添加的物品堆栈，会被修改（如果有剩余）
     * @return 实际存入的数量
     */
    public int addItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) return 0;
        int count = itemStack.getCount();
        if (storageItems.containsKey(itemStack)) {
            long storageCount = storageItems.get(itemStack);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= itemStack.getCount()) {
                storageItems.replace(itemStack, storageCount + itemStack.getCount());
                onItemChanged(itemStack, false);
                return count;
            } else {
                storageItems.replace(itemStack, Long.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - (int) remainingSpaces);
                onItemChanged(itemStack, false);
                return (int) remainingSpaces;
            }
        } else {
            storageItems.put(itemStack, (long) itemStack.getCount());
            onItemChanged(itemStack, true);
            return count;
        }
    }

    /**
     * 添加指定数量的物品到存储中
     * 
     * @param itemId 物品堆栈
     * @param count 要添加的数量
     * @return 实际成功添加的数量
     */
    public long addItem(ItemStack itemId, long count) {
        if (itemId.isEmpty() || count == 0) return 0L;
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= count) {
                storageItems.replace(itemId, storageCount + count);
                onItemChanged(itemId, false);
                return count;
            } else {
                storageItems.replace(itemId, Long.MAX_VALUE);
                onItemChanged(itemId, false);
                return remainingSpaces;
            }
        } else {
            storageItems.put(itemId, count);
            onItemChanged(itemId, true);
            return count;
        }
    }

    /**
     * 填充物品叠堆，不限制数量
     * 
     * @param itemStack 要填充的物品
     * @param count 要填充的数量，负数为扣除
     */
    public void fillItemStack(ItemStack itemStack, int count) {
        if (itemStack.isEmpty() || count == 0) return;
        if (storageItems.containsKey(itemStack)) {
            long storageCount = storageItems.get(itemStack);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (count >= storageCount) {
                storageItems.remove(itemStack);
                itemStack.setCount(itemStack.getCount() + (int) storageCount);
                onItemChanged(itemStack, true);
            } else if (remainingSpaces < -count) {
                storageItems.replace(itemStack, Long.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - (int) remainingSpaces);
                onItemChanged(itemStack, false);
            } else {
                storageItems.replace(itemStack, storageCount - count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(itemStack, false);
            }
        } else {
            if (count < 0) {
                storageItems.put(itemStack, (long) -count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(itemStack, true);
            }
        }
    }

    /**
     * 取出指定数量的物品（不限制数量）
     * 
     * @param itemId 要取出的物品
     * @param count 要取出的数量
     * @return 包含指定数量的新物品堆栈，如果无法取出则返回空堆栈
     */
    public ItemStack takeItem(ItemStack itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.isEmpty() || count == 0) return ItemStack.EMPTY;
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = (int) storageCount;
            onItemChanged(itemId, true);
        }
        return itemId.copyWithCount(count);
    }

    /**
     * 安全地取出物品（数量限制在叠堆最大值内）
     * 
     * @param itemId 要取出的物品
     * @param count 要取出的数量
     * @return 包含指定数量的新物品堆栈，如果无法取出则返回空堆栈
     */
    public ItemStack saveTakeItem(ItemStack itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.isEmpty() || count == 0) return ItemStack.EMPTY;
        ItemStack itemStack = itemId.copyWithCount(1);
        count = Integer.min(count, itemStack.getMaxStackSize());
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = (int) storageCount;
            onItemChanged(itemId, true);
        }
        itemStack.setCount(count);
        return itemStack;
    }

    /**
     * 安全地取出物品（可选择取一半）
     * 
     * @param itemId 要取出的物品
     * @param half 是否只取一半数量
     * @return 包含指定数量的新物品堆栈，如果无法取出则返回空堆栈
     */
    public ItemStack saveTakeItem(ItemStack itemId, boolean half) {
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
        ItemStack itemStack = itemId.copyWithCount(1);
        int count = half ? (itemStack.getMaxStackSize() + 1) / 2 : itemStack.getMaxStackSize();
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = (int) storageCount;
            onItemChanged(itemId, true);
        }
        itemStack.setCount(count);
        return itemStack;
    }

    /**
     * 移除指定物品堆栈中的物品
     * 
     * @param itemStack 要移除的物品堆栈
     */
    public void removeItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) return;
        if (!storageItems.containsKey(itemStack)) return;
        long storageCount = storageItems.get(itemStack);
        if (itemStack.getCount() < storageCount) {
            storageItems.replace(itemStack, storageCount - itemStack.getCount());
            onItemChanged(itemStack, false);
        } else {
            storageItems.remove(itemStack);
            onItemChanged(itemStack, true);
        }
    }

    /**
     * 移除指定数量的物品
     * 
     * @param itemId 要移除的物品
     * @param count 要移除的数量
     */
    public void removeItem(ItemStack itemId, long count) {
        if (!storageItems.containsKey(itemId)) return;
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            onItemChanged(itemId, true);
        }
    }

    /**
     * 检查存储是否为空
     * 
     * @return 如果存储为空则返回true，否则返回false
     */
    public boolean isEmpty() {
        return storageItems.isEmpty();
    }

    /**
     * 获取槽位总数
     * 
     * @return 存储物品种类数加上54（固定的槽位数）
     */
    @Override
    public int getSlots() {
        return storageItems.size() + 54;
    }

    /**
     * 获取指定槽位的物品堆栈
     * 
     * @param slot 槽位索引
     * @return 对应的物品堆栈，如果槽位无效则返回空堆栈
     */
    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot >= slotItemTemp.size() + 27 || slot < 27) return ItemStack.EMPTY;
        ItemStack itemStack = slotItemTemp.get(slot - 27);
        itemStack.setCount((int) Math.min(Integer.MAX_VALUE, storageItems.get(itemStack)));
        return itemStack;
    }

    /**
     * 向指定槽位插入物品
     * 
     * @param slot 槽位索引
     * @param stack 要插入的物品堆栈
     * @param simulate 是否为模拟操作
     * @return 剩余的物品堆栈（如果有）
     */
    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return stack;
        ItemStack remainingStack = ItemStack.EMPTY;
        if (storageItems.containsKey(stack)) {
            long storageCount = storageItems.get(stack);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= stack.getCount()) {
                if (!simulate) storageItems.replace(stack, storageCount + stack.getCount());
            } else {
                if (!simulate) storageItems.replace(stack, Long.MAX_VALUE);
                remainingStack = stack.copy();
                remainingStack.setCount(stack.getCount() - (int) remainingSpaces);
            }
            if (!simulate) onItemChanged(stack, false);
        } else {
            if (!simulate) {
                storageItems.put(stack, (long) stack.getCount());
                onItemChanged(stack, true);
            }
        }
        return remainingStack;
    }


    /**
     * 从指定槽位提取物品
     * 
     * @param slot 槽位索引
     * @param amount 要提取的数量
     * @param simulate 是否为模拟操作
     * @return 提取的物品堆栈，如果无法提取则返回空堆栈
     */
    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= slotItemTemp.size() + 27 || slot < 27) return ItemStack.EMPTY;
        ItemStack itemId = slotItemTemp.get(slot - 27);
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
        ItemStack itemStack = itemId.copyWithCount(1);
        int count = Math.min(itemStack.getMaxStackSize(), amount);
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            if (!simulate) {
                storageItems.replace(itemId, storageCount - count);
                onItemChanged(itemId, false);
            }
        } else {
            if (!simulate) {
                storageItems.remove(itemId);
                onItemChanged(itemId, true);
            }
            count = (int) storageCount;
        }
        itemStack.setCount(count);
        return itemStack;
    }

    /**
     * 获取槽位容量限制
     * 
     * @param slot 槽位索引
     * @return 槽位容量限制（Integer.MAX_VALUE）
     */
    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    /**
     * 检查指定槽位是否可以接受指定物品
     * 
     * @param slot 槽位索引
     * @param stack 物品堆栈
     * @return 如果可以接受则返回true，否则返回false
     */
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return !stack.isEmpty();
    }

}