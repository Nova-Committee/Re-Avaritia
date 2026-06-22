package committee.nova.mods.avaritia.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Slot for infinity chest storage. It follows the backing container stack limit
 * so a single slot can hold oversized stacks.
 */
public class InfinitySlot extends Slot {
    public InfinitySlot(Container container, int slotId, int x, int y) {
        super(container, slotId, x, y);
    }

    @Override
    public int getMaxStackSize() {
        return this.container.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.max(this.getMaxStackSize(), stack.getMaxStackSize());
    }
}
