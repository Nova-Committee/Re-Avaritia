package committee.nova.mods.avaritia.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InfinitySlot extends Slot {

    public InfinitySlot(Container container, int slotId, int pX, int pY) {
        super(container, slotId, pX, pY);
    }

    @Override
    public int getMaxStackSize() {
        return this.container.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.max(this.getMaxStackSize(), stack.getMaxStackSize());
    }}
