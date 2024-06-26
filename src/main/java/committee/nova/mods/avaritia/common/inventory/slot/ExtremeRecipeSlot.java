package committee.nova.mods.avaritia.common.inventory.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * FilterSlot
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/21 下午9:15
 */
public class ExtremeRecipeSlot extends SlotItemHandler {
    public ExtremeRecipeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return false;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        return ItemHandlerHelper.copyStackWithSize(super.getItem(), 1);
    }
}
