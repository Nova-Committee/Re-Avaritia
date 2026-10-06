package committee.nova.mods.avaritia.api.common.wrapper;

import net.minecraft.world.item.ItemStack;

public interface ItemHandler {
    int getSlots();
    ItemStack getStackInSlot(int slot);
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    ItemStack extractItem(int slot, int amount, boolean simulate);
    int getSlotLimit(int slot);
    boolean isItemValid(int slot, ItemStack stack);
}
