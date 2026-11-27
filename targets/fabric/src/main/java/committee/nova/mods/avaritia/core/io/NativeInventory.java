package committee.nova.mods.avaritia.core.io;

import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import committee.nova.mods.avaritia.api.common.wrapper.ModifiableItemHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Vanilla hopper/container view over the same storage used by menus and routing. */
public interface NativeInventory extends WorldlyContainer, ItemStorageProvider {
    int[] EMPTY_SLOTS = new int[0];
    static int[] slotIndices(int count, int[] cached) {
        if (cached.length == count) return cached;
        int[] slots = new int[count];
        for (int i = 0; i < count; i++) slots[i] = i;
        return slots;
    }
    default ItemHandler inventoryHandler() { return getItemHandler((Direction) null); }
    @Override default int getContainerSize() { ItemHandler handler = inventoryHandler(); return handler == null ? 0 : handler.getSlots(); }
    @Override default boolean isEmpty() { ItemHandler handler = inventoryHandler(); if (handler == null) return true; for (int i = 0; i < handler.getSlots(); i++) if (!handler.getStackInSlot(i).isEmpty()) return false; return true; }
    @Override default ItemStack getItem(int slot) { ItemHandler handler = inventoryHandler(); if (handler == null || slot < 0 || slot >= handler.getSlots()) return ItemStack.EMPTY; ItemStack stack = handler.getStackInSlot(slot); return handler instanceof ModifiableItemHandler || stack.isEmpty() ? stack : stack.copyWithCount(Math.min(stack.getCount(), stack.getMaxStackSize())); }
    @Override default ItemStack removeItem(int slot, int amount) { ItemHandler handler = inventoryHandler(); if (handler == null) return ItemStack.EMPTY; ItemStack result = handler.extractItem(slot, amount, false); if (!result.isEmpty()) setChanged(); return result; }
    @Override default ItemStack removeItemNoUpdate(int slot) { return removeItem(slot, Integer.MAX_VALUE); }
    @Override default void setItem(int slot, ItemStack stack) {
        ItemHandler handler = inventoryHandler(); if (handler == null) return;
        if (handler instanceof ModifiableItemHandler modifiable) modifiable.setStackInSlot(slot, stack);
        else {
            ItemStack previous = getItem(slot);
            if (ItemStack.isSameItemSameTags(previous, stack)) {
                int difference = stack.getCount() - previous.getCount();
                if (difference > 0) handler.insertItem(slot, stack.copyWithCount(difference), false);
                else if (difference < 0) handler.extractItem(slot, -difference, false);
            } else {
                if (stack.isEmpty() && !previous.isEmpty()) handler.extractItem(slot, previous.getCount(), false);
                if (!stack.isEmpty()) handler.insertItem(slot, stack, false);
            }
        }
        setChanged();
    }
    @Override default boolean stillValid(Player player) { return true; }
    @Override default void clearContent() {
        ItemHandler handler = inventoryHandler();
        if (handler == null) return;
        if (handler instanceof committee.nova.mods.avaritia.core.channel.Channel channel) {
            for (String key : channel.getItemKeys().clone()) channel.removeItem(key, Long.MAX_VALUE);
        } else if (handler instanceof committee.nova.mods.avaritia.core.chest.ChestHandler chest) {
            for (String key : chest.storageItems.keySet().toArray(new String[0])) chest.removeItem(key, Long.MAX_VALUE);
        } else {
            for (int i = 0; i < handler.getSlots(); i++) handler.extractItem(i, Integer.MAX_VALUE, false);
        }
        setChanged();
    }
    @Override default boolean canPlaceItem(int slot, ItemStack stack) { ItemHandler handler = inventoryHandler(); return handler != null && handler.isItemValid(slot, stack); }
    @Override default boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { ItemHandler handler = getItemHandler(side); return handler != null && handler.isItemValid(slot, stack) && handler.insertItem(slot, stack, true).getCount() < stack.getCount(); }
    @Override default boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { ItemHandler handler = getItemHandler(side); return handler != null && !handler.extractItem(slot, 1, true).isEmpty(); }
}
