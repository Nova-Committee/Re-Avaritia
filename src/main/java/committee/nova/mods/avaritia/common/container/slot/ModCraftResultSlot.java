package committee.nova.mods.avaritia.common.container.slot;

import committee.nova.mods.avaritia.common.container.ModCraftContainer;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

/**
 * Description: {@link net.minecraft.world.inventory.ResultSlot}
 * Author: cnlimiter
 * Date: 2022/2/20 9:45
 * Version: 1.0
 */
public class ModCraftResultSlot extends Slot {

    private final AbstractContainerMenu container;
    private final ModCraftContainer matrix;
    private final Player player;
    private int removeCount;

    public ModCraftResultSlot(Player pPlayer, AbstractContainerMenu menu, ModCraftContainer matrix, Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);
        this.player = pPlayer;
        this.container = menu;
        this.matrix = matrix;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull ItemStack remove(int pAmount) {
        if (this.hasItem()) {
            this.removeCount += Math.min(pAmount, this.getItem().getCount());
        }

        return super.remove(pAmount);
    }

    @Override
    protected void onQuickCraft(@NotNull ItemStack pStack, int pAmount) {
        this.removeCount += pAmount;
        this.checkTakeAchievements(pStack);
    }

    @Override
    protected void onSwapCraft(int pNumItemsCrafted) {
        this.removeCount += pNumItemsCrafted;
    }

    @Override
    public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
        this.checkTakeAchievements(stack);
        var level = player.level();
        var inventory = this.matrix.asCraftInput();
        CommonHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> remaining = level.getRecipeManager().getRemainingItemsFor(ModRecipeTypes.CRAFTING_TABLE_RECIPE.get(), inventory, level);
        CommonHooks.setCraftingPlayer(null);

        for (int k = 0; k < inventory.height(); k++) {
            for (int l = 0; l < inventory.width(); l++) {
                var index = l + inventory.left() + (k + inventory.top()) * this.matrix.getWidth();
                var slotStack = this.matrix.getItem(index);

                if (!slotStack.isEmpty()) {
                    this.matrix.removeItem(index, 1);
                    slotStack = this.matrix.getItem(index);
                }

                var remainingStack = remaining.get(l + k * inventory.width());
                if (!remainingStack.isEmpty()) {
                    if (slotStack.isEmpty()) {
                        this.matrix.setItem(index, remainingStack);
                    } else if (ItemStack.isSameItemSameComponents(slotStack, remainingStack)) {
                        remainingStack.grow(slotStack.getCount());
                        this.matrix.setItem(index, remainingStack);
                    } else if (!player.getInventory().add(remainingStack)) {
                        player.drop(remainingStack, false);
                    }
                }
            }
        }

        this.container.slotsChanged(this.matrix);
    }

    @Override
    protected void checkTakeAchievements(@NotNull ItemStack pStack) {
        if (this.removeCount > 0) {
            pStack.onCraftedBy(this.player.level(), this.player, this.removeCount);
            EventHooks.firePlayerCraftingEvent(this.player, pStack, this.matrix);
        }

        if (this.container instanceof RecipeCraftingHolder recipeholder) {
            recipeholder.awardUsedRecipes(this.player, this.matrix.getItems());
        }

        this.removeCount = 0;
    }
}
