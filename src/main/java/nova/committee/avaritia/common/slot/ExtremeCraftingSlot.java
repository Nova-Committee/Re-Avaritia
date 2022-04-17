package nova.committee.avaritia.common.slot;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nova.committee.avaritia.init.registry.ModRecipe;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/2/20 9:45
 * Version: 1.0
 */
public class ExtremeCraftingSlot extends Slot {

    private final AbstractContainerMenu container;
    private final Container matrix;

    public ExtremeCraftingSlot(AbstractContainerMenu container, Container matrix, Container inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.container = container;
        this.matrix = matrix;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        boolean isVanilla = false;


        NonNullList<ItemStack> remaining;

        remaining = player.level.getRecipeManager().getRemainingItemsFor(ModRecipe.RecipeTypes.CRAFTING, this.matrix, player.level);

        for (int i = 0; i < remaining.size(); i++) {
            var slotStack = this.matrix.getItem(i);
            var remainingStack = remaining.get(i);

            if (!slotStack.isEmpty()) {
                this.matrix.removeItem(i, 1);
                slotStack = this.matrix.getItem(i);
            }

            if (!remainingStack.isEmpty()) {
                if (slotStack.isEmpty()) {
                    this.matrix.setItem(i, remainingStack);
                } else if (ItemStack.isSame(slotStack, remainingStack) && ItemStack.tagMatches(slotStack, remainingStack)) {
                    remainingStack.grow(slotStack.getCount());
                    this.matrix.setItem(i, remainingStack);
                } else if (!player.getInventory().add(remainingStack)) {
                    player.drop(remainingStack, false);
                }
            }
        }

        this.container.slotsChanged(this.matrix);
    }

}
