package committee.nova.mods.avaritia.core.chest;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

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

    public boolean hasItem(ItemSuper item) {
        return storageItems.containsKey(item);
    }

    public int canStorageAmount(ItemStack itemStack) {
        long a = storageItems.getOrDefault(ItemSuper.of(itemStack), 0L);
        if (a == 0L) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    public boolean canStorageItem(ItemStack itemStack) {
        if (storageItems.containsKey(ItemSuper.of(itemStack))) {
            return storageItems.get(ItemSuper.of(itemStack)) < Long.MAX_VALUE;
        } else return true;
    }

    /**
     * @param itemStack 会被修改，塞不进去会有余，
     */
    public void addItem(ItemStack itemStack) {
        var itemSuper = ItemSuper.of(itemStack);
        if (itemStack.isEmpty() || itemSuper == null || itemSuper.getStack().isEmpty()) return;
        if (storageItems.containsKey(itemSuper)) {
            long storageCount = storageItems.get(itemSuper);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= itemStack.getCount()) {
                var actionItemSuper = itemSuper.copyWithCount(storageCount + itemStack.getCount());
                storageItems.replace(actionItemSuper, storageCount + itemStack.getCount());
                itemStack.setCount(0);
                onItemChanged(actionItemSuper, false);
            } else {
                var actionItemSuper = itemSuper.copyWithCount(Long.MAX_VALUE);
                storageItems.replace(actionItemSuper, Long.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - (int) remainingSpaces);
                onItemChanged(actionItemSuper, false);
            }
        } else {
            var actionItemSuper = itemSuper.copyWithCount(itemStack.getCount());
            storageItems.put(actionItemSuper, (long) itemStack.getCount());
            itemStack.setCount(0);
            onItemChanged(actionItemSuper, true);
        }
    }

    /**
     * @return 成功进入的
     */
    public long addItem(ItemSuper itemSuper, long count) {
        if (itemSuper.getStack().isEmpty() || count == 0) return 0L;
        if (storageItems.containsKey(itemSuper)) {
            long storageCount = storageItems.get(itemSuper);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= count) {
                var actionItemSuper = itemSuper.copyWithCount(storageCount + count);
                storageItems.replace(actionItemSuper, storageCount + count);
                onItemChanged(actionItemSuper, false);
                return count;
            } else {
                var actionItemSuper = itemSuper.copyWithCount(Long.MAX_VALUE);
                storageItems.replace(actionItemSuper, Long.MAX_VALUE);
                onItemChanged(actionItemSuper, false);
                return remainingSpaces;
            }
        } else {
            var actionItemSuper = itemSuper.copyWithCount(count);
            storageItems.put(actionItemSuper, count);
            onItemChanged(actionItemSuper, true);
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
        var itemSuper = ItemSuper.of(itemStack);
        if (itemStack.isEmpty() || itemSuper == null || count == 0) return;
        if (storageItems.containsKey(itemSuper)) {
            long storageCount = storageItems.get(itemSuper);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (count >= storageCount) {
                storageItems.remove(itemSuper);
                itemStack.setCount(itemStack.getCount() + (int) storageCount);
                onItemChanged(itemSuper, true);
            } else if (remainingSpaces < -count) {
                var actionItemSuper = itemSuper.copyWithCount(Long.MAX_VALUE);
                storageItems.replace(actionItemSuper, Long.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - (int) remainingSpaces);
                onItemChanged(actionItemSuper, false);
            } else {
                var actionItemSuper = itemSuper.copyWithCount(storageCount - count);
                storageItems.replace(actionItemSuper, storageCount - count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(actionItemSuper, false);
            }
        } else {
            if (count < 0) {
                var actionItemSuper = itemSuper.copyWithCount(-count);
                storageItems.put(actionItemSuper, (long) -count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(actionItemSuper, true);
            }
        }
    }

    /**
     * 获取物品，但不限制数量。
     */
    public ItemStack takeItem(ItemSuper itemSuper, int count) {
        if (!storageItems.containsKey(itemSuper) || itemSuper.getStack().isEmpty() || count == 0) return ItemStack.EMPTY;
        long storageCount = storageItems.get(itemSuper);
        if (count < storageCount) {
            var actionItemSuper = itemSuper.copyWithCount(storageCount -count);
            storageItems.replace(actionItemSuper, storageCount - count);
            onItemChanged(actionItemSuper, false);
        } else {
            storageItems.remove(itemSuper);
            count = (int) storageCount;
            onItemChanged(itemSuper, true);
        }
        return itemSuper.getStack().copyWithCount(count);
    }

    /**
     * 获取物品，数量限制在叠堆最大值。
     */
    public ItemStack saveTakeItem(ItemSuper itemSuper, int count) {
        if (!storageItems.containsKey(itemSuper) || itemSuper.getStack().isEmpty() || count == 0) return ItemStack.EMPTY;
        ItemStack itemStack = itemSuper.getStack();
        count = Integer.min(count, itemStack.getMaxStackSize());
        long storageCount = storageItems.get(itemSuper);
        if (count < storageCount) {
            var actionItemSuper = itemSuper.copyWithCount(storageCount -count);
            storageItems.replace(actionItemSuper, storageCount - count);
            onItemChanged(actionItemSuper, false);
        } else {
            storageItems.remove(itemSuper);
            count = (int) storageCount;
            onItemChanged(itemSuper, true);
        }
        itemStack.setCount(count);
        return itemStack;
    }

    public ItemStack saveTakeItem(ItemSuper itemSuper, boolean half) {
        if (!storageItems.containsKey(itemSuper)) return ItemStack.EMPTY;
        ItemStack itemStack = itemSuper.getStack();
        int count = half ? (itemStack.getMaxStackSize() + 1) / 2 : itemStack.getMaxStackSize();
        long storageCount = storageItems.get(itemSuper);
        if (count < storageCount) {
            var actionItemSuper = itemSuper.copyWithCount(storageCount -count);
            storageItems.replace(actionItemSuper, storageCount - count);
            onItemChanged(actionItemSuper, false);
        } else {
            storageItems.remove(itemSuper);
            count = (int) storageCount;
            onItemChanged(itemSuper, true);
        }
        itemStack.setCount(count);
        return itemStack;
    }

    public void removeItem(ItemStack itemStack) {
        var itemSuper = ItemSuper.of(itemStack);
        if (itemStack.isEmpty() || itemSuper == null) return;
        if (!storageItems.containsKey(itemSuper)) return;
        long storageCount = storageItems.get(itemSuper);
        if (itemStack.getCount() < storageCount) {
            var actionItemSuper = itemSuper.copyWithCount(storageCount - itemStack.getCount());
            storageItems.replace(actionItemSuper, storageCount - itemStack.getCount());
            onItemChanged(actionItemSuper, false);
        } else {
            storageItems.remove(itemSuper);
            onItemChanged(itemSuper, true);
        }
    }

    public void removeItem(ItemSuper itemSuper, long count) {
        if (!storageItems.containsKey(itemSuper)) return;
        long storageCount = storageItems.get(itemSuper);
        if (count < storageCount) {
            var actionItemSuper = itemSuper.copyWithCount(storageCount - count);
            storageItems.replace(itemSuper, storageCount - count);
            onItemChanged(itemSuper, false);
        } else {
            storageItems.remove(itemSuper);
            onItemChanged(itemSuper, true);
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
        var itemSuper = ItemSuper.of(itemStack);
        if (itemStack.isEmpty() || itemSuper == null) return itemStack;
        ItemStack remainingStack = ItemStack.EMPTY;
        if (storageItems.containsKey(itemSuper)) {
            long storageCount = storageItems.get(itemSuper);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= itemStack.getCount()) {
                if (!simulate) {
                    var actionItemSuper = itemSuper.copyWithCount(storageCount + itemStack.getCount());
                    storageItems.replace(actionItemSuper, storageCount + itemStack.getCount());
                    onItemChanged(actionItemSuper, false);
                }
            } else {
                if (!simulate) {
                    var actionItemSuper = itemSuper.copyWithCount(Long.MAX_VALUE);
                    storageItems.replace(actionItemSuper, Long.MAX_VALUE);
                    onItemChanged(actionItemSuper, false);
                }
                remainingStack = itemStack.copy();
                remainingStack.setCount(itemStack.getCount() - (int) remainingSpaces);
            }
        } else {
            if (!simulate) {
                var actionItemSuper = itemSuper.copyWithCount(itemStack.getCount());
                storageItems.put(actionItemSuper, (long) itemStack.getCount());
                onItemChanged(actionItemSuper, true);
            }
        }
        return remainingStack;
    }


    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= itemKeys.length + 27 || slot < 27) return ItemStack.EMPTY;
        var itemSuper = slotItemTemp[slot - 27];
        var itemStack = itemSuper.getStack();
        if (!storageItems.containsKey(itemSuper)) return ItemStack.EMPTY;
        int count = Math.min(itemStack.getMaxStackSize(), amount);
        long storageCount = storageItems.get(itemSuper);
        if (count < storageCount) {
            if (!simulate) {
                var actionItemSuper = itemSuper.copyWithCount(storageCount - count);
                storageItems.replace(actionItemSuper, storageCount - count);
                onItemChanged(actionItemSuper, false);
            }
        } else {
            if (!simulate) {
                storageItems.remove(itemSuper);
                onItemChanged(itemSuper, true);
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
