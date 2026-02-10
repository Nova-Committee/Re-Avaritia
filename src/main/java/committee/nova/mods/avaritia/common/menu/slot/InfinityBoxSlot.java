package committee.nova.mods.avaritia.common.menu.slot;

import committee.nova.mods.avaritia.common.block.chest.InfinityChestBlock;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Locale;

public class InfinityBoxSlot extends Slot {
    public InfinityBoxSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem){
            Block block = ((BlockItem) stack.getItem()).getBlock();
            return !(block instanceof InfinityChestBlock);
        } else if (!stack.isEmpty()) {
            String name = stack.getDescriptionId().toLowerCase(Locale.ENGLISH);
            return !name.contains("pouch") && !name.contains("bag") && !name.contains("strongbox") && !name.contains("shulker_box");
        }
        return true;
    }

    @Override
    public void set(ItemStack stack) {
        ItemStack itemStack = this.container.getItem(this.getSlotIndex());
        if (itemStack.getMaxStackSize() <= 1) super.set(stack);
        else {
            this.container.setItem(this.getSlotIndex(), stack);
            this.setChanged();
        }
        super.set(stack);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        int size = stack.getMaxStackSize();
        return size <= 1 ? 1 : this.getMaxStackSize();
    }

    @Override
    protected void onSwapCraft(int i) {
        super.onSwapCraft(i);
    }

}