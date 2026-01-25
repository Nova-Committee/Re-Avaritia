package committee.nova.mods.avaritia.api.common.slot;

import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class ItemStackWrapperSlot extends SlotItemHandler {
    private final ItemStackWrapper inventory;
    private final int index;

    public ItemStackWrapperSlot(ItemStackWrapper inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.inventory = inventory;
        this.index = index;
    }

    @Override
    public boolean mayPickup(Player player) {
        return !this.inventory.extractItem(this.index, 1, true, true).isEmpty();
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        return this.inventory.extractItem(this.index, amount, false, true);
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        var slotLimit = this.inventory.getSlotLimit(this.index);
        if (slotLimit > 64) {
            // if the max size for the stack is less than 64 then we should decrease the max stack size to the
            // same ratio
            if (stack.getMaxStackSize() < 64) {
                return (int) (slotLimit * (float) stack.getMaxStackSize() / 64);
            }

            return slotLimit;
        }

        return super.getMaxStackSize(stack);
    }
}
