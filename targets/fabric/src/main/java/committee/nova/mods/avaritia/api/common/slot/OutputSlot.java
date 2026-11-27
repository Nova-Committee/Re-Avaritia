package committee.nova.mods.avaritia.api.common.slot;

import net.minecraft.world.item.ItemStack;
import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import committee.nova.mods.avaritia.api.common.slot.HandlerSlot;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 15:16
 * Version: 1.0
 */
public class OutputSlot extends HandlerSlot {
    public OutputSlot(ItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }
}
