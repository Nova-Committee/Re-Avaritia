package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * @author: cnlimiter
 */
public abstract class ChestHandler implements IItemHandler {
    public final HashMap<String, Long> storageItems = new HashMap<>();
    private ItemStack[] slotItemTemp = {ItemStack.EMPTY};
    private String[] itemKeys = new String[]{};

    public ChestHandler() {}

    public abstract boolean isRemoved();

    public void onItemChanged(String itemId, boolean listChanged) {
        if (listChanged) updateItemKeys();
    }

    public void updateItemKeys() {
        itemKeys = storageItems.keySet().toArray(new String[]{});
        slotItemTemp = new ItemStack[itemKeys.length];
        for (int i = 0; i < itemKeys.length; i++) slotItemTemp[i] = new ItemStack(StorageUtils.getItem(itemKeys[i]));
    }

    public boolean hasItem(String item) {
        return storageItems.containsKey(item);
    }

    public int getItemAmount(String item) {
        return (int) Long.min(Integer.MAX_VALUE, storageItems.getOrDefault(item, 0L));
    }

    public long getRealItemAmount(String item) {
        return storageItems.getOrDefault(item, 0L);
    }

    public int getStorageAmount(Item item) {
        return (int) Long.min(Integer.MAX_VALUE, storageItems.getOrDefault(StorageUtils.getItemId(item), 0L));
    }

    public int canStorageAmount(ItemStack itemStack) {
        if (itemStack.hasTag()) return 0;
        long a = storageItems.getOrDefault(StorageUtils.getItemId(itemStack.getItem()), 0L);
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
        if (itemStack.hasTag() || itemStack.isEmpty()) return 0;
        String itemId = StorageUtils.getItemId(itemStack.getItem());
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
    public long addItem(String itemId, long count) {
        if (itemId.equals("minecraft:air") || count == 0) return 0L;
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
        if (itemStack.isEmpty() || count == 0 || itemStack.hasTag()) return;
        String itemId = StorageUtils.getItemId(itemStack.getItem());
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
    public ItemStack takeItem(String itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.equals("minecraft:air") || count == 0) return ItemStack.EMPTY;
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = (int) storageCount;
            onItemChanged(itemId, true);
        }
        return new ItemStack(StorageUtils.getItem(itemId), count);
    }

    /**
     * 获取物品，数量限制在叠堆最大值。
     */
    public ItemStack saveTakeItem(String itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.equals("minecraft:air") || count == 0) return ItemStack.EMPTY;
        ItemStack itemStack = new ItemStack(StorageUtils.getItem(itemId), 1);
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

    public ItemStack saveTakeItem(String itemId, boolean half) {
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
        ItemStack itemStack = new ItemStack(StorageUtils.getItem(itemId), 1);
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
        String itemId = StorageUtils.getItemId(itemStack.getItem());
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

    public void removeItem(String itemId, long count) {
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
        if (slot >= itemKeys.length + 27 || slot < 27) return ItemStack.EMPTY;
        ItemStack itemStack = slotItemTemp[slot - 27];
        itemStack.setCount((int) Math.min(Integer.MAX_VALUE, storageItems.get(itemKeys[slot - 27])));
        return itemStack;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || stack.hasTag()) return stack;
        String itemId = StorageUtils.getItemId(stack.getItem());
        ItemStack remainingStack = ItemStack.EMPTY;
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= stack.getCount()) {
                if (!simulate) storageItems.replace(itemId, storageCount + stack.getCount());
            } else {
                if (!simulate) storageItems.replace(itemId, Long.MAX_VALUE);
                remainingStack = stack.copy();
                remainingStack.setCount(stack.getCount() - (int) remainingSpaces);
            }
            if (!simulate) onItemChanged(itemId, false);
        } else {
            if (!simulate) {
                storageItems.put(itemId, (long) stack.getCount());
                onItemChanged(itemId, true);
            }
        }
        return remainingStack;
    }


    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= itemKeys.length + 27 || slot < 27) return ItemStack.EMPTY;
        String itemId = itemKeys[slot - 27];
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
        ItemStack itemStack = new ItemStack(StorageUtils.getItem(itemId), 1);
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
        return !stack.isEmpty() && !stack.hasTag();
    }

}
