package committee.nova.mods.avaritia.common.container.slot;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * FilterSlot
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/6/21 下午9:15
 */
public class ModCraftRecipeSlot extends SlotItemHandler {
    public ModCraftRecipeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
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
