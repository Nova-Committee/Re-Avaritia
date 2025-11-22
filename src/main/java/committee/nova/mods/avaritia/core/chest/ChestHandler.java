package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.api.utils.ItemUtils;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cnlimiter
 */
public abstract class ChestHandler implements IItemHandler {
    public final HashMap<ItemSuper, Long> storageItems = new HashMap<>();
    private ItemSuper[] slotItemTemp = {ItemSuper.EMPTY};
    private String[] itemKeys = new String[]{};

    public ChestHandler() {}

    public abstract boolean isRemoved();

    public void onItemChanged(ItemSuper itemId, boolean listChanged) {
        if (listChanged) updateItemKeys();
    }

    public void updateItemKeys() {
        itemKeys = storageItems.keySet().stream().map(ItemSuper::toString).toArray(String[]::new);
        slotItemTemp = storageItems.keySet().toArray(ItemSuper[]::new);
    }

    public boolean hasItem(String item) {
        return storageItems.containsKey(item);
    }

    public int getItemAmount(String item) {
        return (int) Long.min(Integer.MAX_VALUE, storageItems.getOrDefault(item, 0L));
    }

    public long getRealItemAmount(ItemSuper item) {
        return storageItems.getOrDefault(item, 0L);
    }

    public int getStorageAmount(ItemSuper item) {
        // 累加所有具有相同基础ID的物品（包括NBT变体）
        long total = 0;
        for (Map.Entry<ItemSuper, Long> entry : storageItems.entrySet()) {
            ItemSuper key = entry.getKey();
            if (key.equals(item)) {
                total += entry.getValue();
            }
        }
        return (int) Long.min(Integer.MAX_VALUE, total);
    }

    public int canStorageAmount(ItemStack itemStack) {
        long a = storageItems.getOrDefault(ItemSuper.of(itemStack), 0L);
        if (a == 0L) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    public boolean canStorageItem(String item) {
        if (storageItems.containsKey(item)) {
            return storageItems.get(item) < Long.MAX_VALUE;
        } else return true;
    }

    public int canStorageItemAmount(String item) {
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
        var itemId = ItemSuper.of(itemStack);
        int count = itemStack.getCount();
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= itemStack.getCount()) {
                storageItems.replace(itemId, storageCount + itemStack.getCount());
                itemStack.setCount(0);
                onItemChanged(itemId, false);
                return count;
            } else {
                storageItems.replace(itemId, Long.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - (int) remainingSpaces);
                onItemChanged(itemId, false);
                return (int) remainingSpaces;
            }
        } else {
            storageItems.put(itemId, (long) itemStack.getCount());
            itemStack.setCount(0);
            onItemChanged(itemId, true);
            return count;
        }
    }

    /**
     * @return 成功进入的
     */
    public long addItem(ItemSuper itemId, long count) {
        if (itemId.getStack().isEmpty() || count == 0) return 0L;
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
        var itemId = ItemSuper.of(itemStack);
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (count >= storageCount) {
                storageItems.remove(itemId);
                itemStack.setCount(itemStack.getCount() + (int) storageCount);
                onItemChanged(itemId, true);
            } else if (remainingSpaces < -count) {
                storageItems.replace(itemId, Long.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - (int) remainingSpaces);
                onItemChanged(itemId, false);
            } else {
                storageItems.replace(itemId, storageCount - count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(itemId, false);
            }
        } else {
            if (count < 0) {
                storageItems.put(itemId, (long) -count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(itemId, true);
            }
        }
    }

    /**
     * 获取物品，但不限制数量。
     */
    public ItemStack takeItem(ItemSuper itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.getStack().isEmpty() || count == 0) return ItemStack.EMPTY;
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = (int) storageCount;
            onItemChanged(itemId, true);
        }
        return itemId.getStack().copyWithCount(count);
    }

    /**
     * 获取物品，数量限制在叠堆最大值。
     */
    public ItemStack saveTakeItem(ItemSuper itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.getStack().isEmpty() || count == 0) return ItemStack.EMPTY;
        ItemStack itemStack = itemId.getStack();
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

    public ItemStack saveTakeItem(ItemSuper itemId, boolean half) {
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
        ItemStack itemStack = itemId.getStack();
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
        var itemId = ItemSuper.of(itemStack);
        if (!storageItems.containsKey(itemId)) return;
        long storageCount = storageItems.get(itemId);
        if (itemStack.getCount() < storageCount) {
            storageItems.replace(itemId, storageCount - itemStack.getCount());
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            onItemChanged(itemId, true);
        }
    }

    public void removeItem(ItemSuper itemId, long count) {
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
        //System.out.println(slot);
        if (slot >= itemKeys.length + 27 || slot < 27) return ItemStack.EMPTY;
        ItemStack itemStack = slotItemTemp[slot - 27].getStack().copy();
        itemStack.setCount((int) Math.min(Integer.MAX_VALUE, storageItems.get(slotItemTemp[slot - 27])));
        return itemStack;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack itemStack, boolean simulate) {
        if (itemStack.isEmpty()) return itemStack;
        var itemId = ItemSuper.of(itemStack);
        ItemStack remainingStack = ItemStack.EMPTY;
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= itemStack.getCount()) {
                if (!simulate) storageItems.replace(itemId, storageCount + itemStack.getCount());
            } else {
                if (!simulate) storageItems.replace(itemId, Long.MAX_VALUE);
                remainingStack = itemStack.copy();
                remainingStack.setCount(itemStack.getCount() - (int) remainingSpaces);
            }
            if (!simulate) onItemChanged(itemId, false);
        } else {
            if (!simulate) {
                storageItems.put(itemId, (long) itemStack.getCount());
                onItemChanged(itemId, true);
            }
        }
        return remainingStack;
    }


    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= itemKeys.length + 27 || slot < 27) return ItemStack.EMPTY;
        var itemId = slotItemTemp[slot - 27];
        var itemStack = itemId.getStack();
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
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
