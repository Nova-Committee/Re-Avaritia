package committee.nova.mods.avaritia.common.wrappers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/1/28 02:22
 * @Description:
 */
public class StorageItem {
    public static final StorageItem EMPTY = new StorageItem(ItemStack.EMPTY, 0L);
    private boolean empty;
    private final ItemStack stack;
    private long count;


    private StorageItem(ItemStack stack, long count) {
        this.stack = stack;
        this.count = count;
    }

    private StorageItem(CompoundTag nbt) {
        this.stack = ItemStack.of(nbt.getCompound("Stack"));
        this.count = Integer.toUnsignedLong(nbt.getInt("Count"));
    }

    public static StorageItem create(ItemStack stack, int count) {
        if (!stack.isEmpty() && count != 0) {
            ItemStack copy = ItemHandlerHelper.copyStackWithSize(stack, 1);
            StorageItem container = new StorageItem(copy, count);
            container.updateEmptyState();
            return container;
        } else {
            return EMPTY;
        }
    }

    public static StorageItem create(ItemStack stack) {
        return create(stack, stack.getCount());
    }

    public static StorageItem read(CompoundTag nbt) {
        StorageItem container = new StorageItem(nbt);
        container.updateEmptyState();
        return container;
    }

    private void updateEmptyState() {
        this.empty = this.isEmpty();
    }

    public boolean isEmpty() {
        if (this == EMPTY) {
            return true;
        } else if (this.stack.isEmpty()) {
            return true;
        } else {
            return this.count <= 0L;
        }
    }

    public ItemStack getStack() {
        return this.empty ? ItemStack.EMPTY : this.stack;
    }

    public long getCount() {
        return this.empty ? 0L : this.count;
    }

    public void setCount(long count) {
        this.count = count;
        this.updateEmptyState();
    }

    public void grow(long count) {
        this.setCount(this.count + count);
    }

    public void shrink(long count) {
        this.grow(-count);
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("Stack", this.stack.serializeNBT());
        nbt.putInt("Count", (int) this.count);
        return nbt;
    }
}
