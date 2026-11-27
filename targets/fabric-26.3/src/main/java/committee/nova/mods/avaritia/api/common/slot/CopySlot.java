package committee.nova.mods.avaritia.api.common.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import committee.nova.mods.avaritia.api.common.slot.HandlerSlot;

/**
 * @author cnlimiter
 */
public class CopySlot extends HandlerSlot {
    public int slotIndex;

    public CopySlot(ItemHandler itemHandler, int index, int x, int y) {
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
