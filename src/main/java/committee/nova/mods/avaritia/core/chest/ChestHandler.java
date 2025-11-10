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
 * @author cnlimiter
 */
public abstract class ChestHandler implements IItemHandler {
    public final HashMap<ItemStack, Long> storageItems = new HashMap<>();
    private List<ItemStack> slotItemTemp = Lists.newArrayList();

    public ChestHandler() {}

    public abstract boolean isRemoved();

    public void onItemChanged(ItemStack itemId, boolean listChanged) {
        if (listChanged) updateItemKeys();
    }

    public void updateItemKeys() {
        slotItemTemp.addAll(storageItems.keySet());
    }

    public boolean hasItem(ItemStack item) {
        return storageItems.containsKey(item);
    }

    public int getItemAmount(ItemStack item) {
        return (int) Long.min(Integer.MAX_VALUE, storageItems.getOrDefault(item, 0L));
    }

    public long getRealItemAmount(ItemStack item) {
        return storageItems.getOrDefault(item, 0L);
    }

    public int getStorageAmount(ItemStack item) {
        return (int) Long.min(Integer.MAX_VALUE, storageItems.getOrDefault(item, 0L));
    }

    public int canStorageAmount(ItemStack itemStack) {
//        if (itemStack.hasTag()) return 0;
        long a = storageItems.getOrDefault(itemStack, 0L);
        if (a == 0L) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    public boolean canStorageItem(ItemStack item) {
        if (storageItems.containsKey(item)) {
            return storageItems.get(item) < Long.MAX_VALUE;
        } else return true;
    }

    public int canStorageItemAmount(ItemStack item) {
        long a = storageItems.getOrDefault(item, 0L);
        if (a == 0L) {
           return Integer.MAX_VALUE;
        }
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    /**
     * @param itemStack 会被修改，塞不进去会有余，
     * @return 存进去的量
     */
    public int addItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) return 0;
        int count = itemStack.getCount();
        if (storageItems.containsKey(itemStack)) {
            long storageCount = storageItems.get(itemStack);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= itemStack.getCount()) {
                storageItems.replace(itemStack, storageCount + itemStack.getCount());
                itemStack.setCount(0);
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
     * @return 成功进入的
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
     * 填充物品叠堆，不限制数量。
     *
     * @param itemStack 要填充的物品
     * @param count     要填充的数量，负数为扣除。
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
     * 获取物品，但不限制数量。
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
     * 获取物品，数量限制在叠堆最大值。
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

    public boolean isEmpty() {
        return storageItems.isEmpty();
    }

    @Override
    public int getSlots() {
        return storageItems.size() + 54;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot >= slotItemTemp.size() + 27 || slot < 27) return ItemStack.EMPTY;
        ItemStack itemStack = slotItemTemp.get(slot - 27);
        itemStack.setCount((int) Math.min(Integer.MAX_VALUE, storageItems.get(itemStack)));
        return itemStack;
    }

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

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return !stack.isEmpty();
    }

}
