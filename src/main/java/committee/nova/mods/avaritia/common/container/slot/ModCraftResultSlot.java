package committee.nova.mods.avaritia.common.container.slot;

import committee.nova.mods.avaritia.common.menu.TierCraftMenu;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Prediction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Description: {@link net.minecraft.world.inventory.ResultSlot}
 * @author cnlimiter
 * Date: 2022/2/20 9:45
 * Version: 1.0
 */
public class ModCraftResultSlot extends Slot {

    private final AbstractContainerMenu container;
    private final CraftingContainer craftSlots;
    private final Player player;
    private int removeCount;

    public ModCraftResultSlot(Player pPlayer, AbstractContainerMenu menu, CraftingContainer craftSlots, Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);
        this.player = pPlayer;
        this.container = menu;
        this.craftSlots = craftSlots;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        return !(this.container instanceof TierCraftMenu menu) || menu.canTakeCraftingResult();
    }

    @Override
    public @NotNull ItemStack remove(int pAmount) {
        if (!this.mayPickup(this.player)) {
            return ItemStack.EMPTY;
        }

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
        CraftingInput input = this.craftSlots.asCraftInput();
        NonNullList<ItemStack> remaining = this.getRemainingItems(player.level(), input);

        for (int i = 0; i < input.size(); i++) {
            var slotStack = this.craftSlots.getItem(i);
            var remainingStack = remaining.get(i);

            if (!slotStack.isEmpty()) {
                this.craftSlots.removeItem(i, 1);
                slotStack = this.craftSlots.getItem(i);
            }

            if (!remainingStack.isEmpty()) {
                if (slotStack.isEmpty()) {
                    this.craftSlots.setItem(i, remainingStack);
                } else if (ItemStack.isSameItemSameComponents(slotStack, remainingStack)) {
                    remainingStack.grow(slotStack.getCount());
                    this.craftSlots.setItem(i, remainingStack);
                } else if (!this.player.getInventory().add(remainingStack)) {
                    this.player.drop(remainingStack, false, Prediction.PREDICTED);
                }
            }
        }
        this.container.slotsChanged(this.craftSlots);
    }

    private NonNullList<ItemStack> getRemainingItems(Level level, CraftingInput input) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess()
                    .getRecipeFor(ModRecipeTypes.CRAFTING_TABLE_RECIPE.get(), input, serverLevel)
                    .map(holder -> holder.value().getRemainingItems(input))
                    .orElseGet(() -> CraftingRecipe.defaultCraftingReminder(input));
        }
        return CraftingRecipe.defaultCraftingReminder(input);
    }

    @Override
    protected void checkTakeAchievements(@NotNull ItemStack pStack) {
        if (this.removeCount > 0) {
            pStack.onCraftedBy(this.player, this.removeCount);
        }

        if (this.container instanceof RecipeCraftingHolder recipeholder) {
            recipeholder.awardUsedRecipes(this.player, this.craftSlots.getItems());
        }

        this.removeCount = 0;
    }
}
