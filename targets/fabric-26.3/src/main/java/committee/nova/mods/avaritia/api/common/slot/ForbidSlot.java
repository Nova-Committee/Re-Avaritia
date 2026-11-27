package committee.nova.mods.avaritia.api.common.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import committee.nova.mods.avaritia.api.common.slot.HandlerSlot;
import org.jetbrains.annotations.NotNull;

/**
 * @author cnlimiter
 */
public class ForbidSlot extends HandlerSlot {
    public ForbidSlot(ItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

}
