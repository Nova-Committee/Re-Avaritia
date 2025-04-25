package committee.nova.mods.avaritia.common.container.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
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
        return super.getItem().copyWithCount(1);
    }
}
