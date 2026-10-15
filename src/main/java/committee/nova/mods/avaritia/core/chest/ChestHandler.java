package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cnlimiter
 */
public abstract class ChestHandler implements ItemHandler {
    public final HashMap<String, Long> storageItems = new HashMap<>();
    // 缓存完整物品快照（数量为1的ItemStack序列化标签），键为身份键
    protected final Map<String, CompoundTag> componentDataCache = new HashMap<>();
    // 旧版存档的 CUSTOM_DATA 缓存（向后兼容，仅用于无完整快照的旧条目）
    protected final Map<String, Tag> nbtDataCache = new HashMap<>();
    private ItemStack[] slotItemTemp = {ItemStack.EMPTY};
    private String[] itemKeys = new String[]{};

    public ChestHandler() {}

    public abstract boolean isRemoved();

    /**
     * 按身份键重建完整物品：优先完整组件快照，其次旧版 CUSTOM_DATA 缓存（旧存档回退）。
     */
    protected ItemStack buildStack(String itemId) {
        CompoundTag snapshot = componentDataCache.get(itemId);
        if (snapshot != null) {
            ItemStack stack = StorageUtils.parseStack(snapshot);
            if (!stack.isEmpty()) return stack;
        }
        ItemStack stack = new ItemStack(StorageUtils.getItem(itemId));
        if (nbtDataCache.get(itemId) instanceof CompoundTag tag) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.copy()));
        }
        return stack;
    }

    /**
     * 新物品种类入库时缓存其完整组件快照。
     */
    protected void cacheSnapshot(String itemId, ItemStack stack) {
        if (componentDataCache.containsKey(itemId) || stack.getComponentsPatch().isEmpty()) return;
        CompoundTag snapshot = StorageUtils.saveStackSnapshot(stack);
        if (snapshot != null) componentDataCache.put(itemId, snapshot);
    }

    protected void dropCaches(String itemId) {
        componentDataCache.remove(itemId);
        nbtDataCache.remove(itemId);
    }

    public ItemStack getItemTemplate(String itemId, int count) {
        ItemStack stack = buildStack(itemId);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        stack.setCount(count);
        return stack;
    }

    public void onItemChanged(String itemId, boolean listChanged) {
        if (listChanged) updateItemKeys();
    }

    public void updateItemKeys() {
        itemKeys = storageItems.keySet().toArray(new String[]{});
        slotItemTemp = new ItemStack[itemKeys.length];
        for (int i = 0; i < itemKeys.length; i++) {
            slotItemTemp[i] = buildStack(itemKeys[i]);
        }
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
        String itemId = StorageUtils.getItemId(item);
        // 累加所有具有相同基础ID的物品（包括NBT变体）
        long total = 0;
        for (Map.Entry<String, Long> entry : storageItems.entrySet()) {
            String key = entry.getKey();
            if (StorageUtils.getBaseItemId(key).equals(itemId)) {
                long count = entry.getValue();
                if (count >= Integer.MAX_VALUE - total) return Integer.MAX_VALUE;
                total += count;
            }
        }
        return (int) Long.min(Integer.MAX_VALUE, total);
    }

    public int canStorageAmount(ItemStack itemStack) {
        String itemId = StorageUtils.getNbtItemId(itemStack);
        long a = storageItems.getOrDefault(itemId, 0L);
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
        String itemId = StorageUtils.getNbtItemId(itemStack);
        int count = itemStack.getCount();

        // 如果是新的带组件物品，保存完整组件快照
        cacheSnapshot(itemId, itemStack);

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
        if (itemStack.isEmpty() || count == 0) return;
        String itemId = StorageUtils.getNbtItemId(itemStack);

        // 如果是新的带组件物品，保存完整组件快照
        if (count < 0) cacheSnapshot(itemId, itemStack);

        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (count >= storageCount) {
                storageItems.remove(itemId);
                dropCaches(itemId);
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
        ItemStack result = buildStack(itemId);
        if (!result.isEmpty()) result.setCount(count);
        if (!storageItems.containsKey(itemId)) dropCaches(itemId);

        return result;
    }

    /**
     * 获取物品，数量限制在叠堆最大值。
     */
    public ItemStack saveTakeItem(String itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.equals("minecraft:air") || count == 0) return ItemStack.EMPTY;
        ItemStack itemStack = buildStack(itemId);
        if (itemStack.isEmpty()) return ItemStack.EMPTY;
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

        if (!storageItems.containsKey(itemId)) dropCaches(itemId);

        return itemStack;
    }

    public ItemStack saveTakeItem(String itemId, boolean half) {
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
        ItemStack itemStack = buildStack(itemId);
        if (itemStack.isEmpty()) return ItemStack.EMPTY;
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

        if (!storageItems.containsKey(itemId)) dropCaches(itemId);

        return itemStack;
    }

    public void removeItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) return;
        String itemId = StorageUtils.getNbtItemId(itemStack);
        if (!storageItems.containsKey(itemId)) return;
        long storageCount = storageItems.get(itemId);
        if (itemStack.getCount() < storageCount) {
            storageItems.replace(itemId, storageCount - itemStack.getCount());
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            dropCaches(itemId);
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
            dropCaches(itemId);
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
        String itemKey = itemKeys[slot - 27];
        ItemStack itemStack = slotItemTemp[slot - 27].copy();
        itemStack.setCount((int) Math.min(Integer.MAX_VALUE, storageItems.get(itemKey)));

        return itemStack;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return stack;
        String itemId = StorageUtils.getNbtItemId(stack);
        ItemStack remainingStack = ItemStack.EMPTY;

        // 如果是新的带组件物品，保存完整组件快照
        if (!simulate) cacheSnapshot(itemId, stack);

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
        if (amount <= 0) return ItemStack.EMPTY;
        if (slot >= itemKeys.length + 27 || slot < 27) return ItemStack.EMPTY;
        String itemId = itemKeys[slot - 27];
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
        ItemStack itemStack = buildStack(itemId);
        if (itemStack.isEmpty()) return ItemStack.EMPTY;
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
                dropCaches(itemId);
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
