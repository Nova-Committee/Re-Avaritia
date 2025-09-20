package committee.nova.mods.avaritia.api.common.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * @author: cnlimiter
 */
public class CopySlot extends SlotItemHandler {
    public int slotIndex;

    public CopySlot(IItemHandler itemHandler, int index, int x, int y) {
        super(itemHandler, index, x, y);
        this.slotIndex = index;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public void set(ItemStack stack) {
        if (!stack.isEmpty() && !mayPlace(stack)) {
            return;
        }
        super.set(stack);
    }
}
