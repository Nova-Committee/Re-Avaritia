package committee.nova.mods.avaritia.common.inventory.slot;

import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Description: {@link net.minecraft.world.inventory.ResultSlot}
 * Author: cnlimiter
 * Date: 2022/2/20 9:45
 * Version: 1.0
 */
public class ExtremeResultSlot extends Slot {

    private final AbstractContainerMenu container;
    private final CraftingContainer craftSlots;
    private final Player player;
    private int removeCount;

    public ExtremeResultSlot(Player pPlayer, AbstractContainerMenu container, CraftingContainer craftSlots, Container inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.player = pPlayer;
        this.container = container;
        this.craftSlots = craftSlots;
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
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> remaining = player.level().getRecipeManager().getRemainingItemsFor(ModRecipeTypes.EXTREME_CRAFT_RECIPE.get(), this.craftSlots, player.level());
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

        for (int i = 0; i < remaining.size(); i++) {
            var slotStack = this.craftSlots.getItem(i);
            var remainingStack = remaining.get(i);

            if (!slotStack.isEmpty()) {
                this.craftSlots.removeItem(i, 1);
                slotStack = this.craftSlots.getItem(i);
            }

            if (!remainingStack.isEmpty()) {
                if (slotStack.isEmpty()) {
                    this.craftSlots.setItem(i, remainingStack);
                } else if (ItemStack.isSameItem(slotStack, remainingStack) && ItemStack.isSameItemSameTags(slotStack, remainingStack)) {
                    remainingStack.grow(slotStack.getCount());
                    this.craftSlots.setItem(i, remainingStack);
                } else if (!this.player.getInventory().add(remainingStack)) {
                    this.player.drop(remainingStack, false);
                }
            }
        }
        this.container.slotsChanged(this.craftSlots);
    }

    @Override
    protected void checkTakeAchievements(@NotNull ItemStack pStack) {
        if (this.removeCount > 0) {
            pStack.onCraftedBy(this.player.level(), this.player, this.removeCount);
            net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(this.player, pStack, this.craftSlots);
        }

        if (this.container instanceof RecipeHolder recipeholder) {
            recipeholder.awardUsedRecipes(this.player, this.craftSlots.getItems());
        }

        this.removeCount = 0;
    }
}
