package committee.nova.mods.avaritia.common.menu;

import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.init.registry.ModBlocks;
import committee.nova.mods.avaritia.init.registry.ModMenus;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/19 12:27
 * @Description:
 */
public class ExtremeSmithingMenu extends ItemCombinerMenu {
    @Nullable
    private SmithingRecipe selectedRecipe;
    private final Level level;
    private final List<ExtremeSmithingRecipe> recipes;

    public ExtremeSmithingMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, ContainerLevelAccess.NULL);
    }

    public ExtremeSmithingMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess access) {
        super(ModMenus.extreme_smithing_table.get(), pContainerId, pPlayerInventory, access);
        this.level = pPlayerInventory.player.level();
        this.recipes = this.level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.EXTREME_SMITHING_RECIPE.get());
    }

    @Override
    protected boolean mayPickup(@NotNull Player pPlayer, boolean pHasStack) {
        return this.selectedRecipe != null && this.selectedRecipe.matches(this.inputSlots, this.level);
    }

    @Override
    protected void onTake(@NotNull Player pPlayer, @NotNull ItemStack pStack) {
        pStack.onCraftedBy(pPlayer.level(), pPlayer, pStack.getCount());
        this.resultSlots.awardUsedRecipes(pPlayer, this.getRelevantItems());
        this.shrinkStackInSlot(0);
        this.shrinkStackInSlot(1);
        this.shrinkStackInSlot(2);
        this.shrinkStackInSlot(3);
        this.shrinkStackInSlot(4);
        this.access.execute((level, pos) -> {
            level.levelEvent(1044, pos, 0);
        });
    }

    @Override
    protected boolean isValidBlock(@NotNull BlockState pState) {
        return pState.is(ModBlocks.extreme_smithing_table.get());
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return true;
    }

    @Override
    public void createResult() {
        List<ExtremeSmithingRecipe> list = this.level.getRecipeManager().getRecipesFor(ModRecipeTypes.EXTREME_SMITHING_RECIPE.get(), this.inputSlots, this.level);
        if (list.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        } else {
            ExtremeSmithingRecipe smithingrecipe = list.get(0);
            ItemStack itemstack = smithingrecipe.assemble(this.inputSlots, this.level.registryAccess());
            //检测无瑕核心...合成给予tag交给你了,我不会)

            if (itemstack.isItemEnabled(this.level.enabledFeatures())) {
                this.selectedRecipe = smithingrecipe;
                this.resultSlots.setRecipeUsed(smithingrecipe);
                this.resultSlots.setItem(0, itemstack);
            }
        }
    }

    @Override
    protected @NotNull ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create().withSlot(0, 31, 35, (stack) -> {
            return this.recipes.stream().anyMatch((recipe) -> {
                return recipe.isTemplateIngredient(stack);
            });
        }).withSlot(1, 49, 35, (stack) -> {
            return this.recipes.stream().anyMatch((recipe) -> {
                return recipe.isBaseIngredient(stack);
            });
        }).withSlot(2, 67, 35, (stack) -> {
            return this.recipes.stream().anyMatch((recipe) -> {
                return recipe.isAdditionIngredient(stack);
            });
        }).withSlot(3, 49, 17, (stack) -> {
            return this.recipes.stream().anyMatch((recipe) -> {
                return recipe.isAdditionIngredient(stack);
            });
        }).withSlot(4, 49, 53, (stack) -> {
            return this.recipes.stream().anyMatch((recipe) -> {
                return recipe.isAdditionIngredient(stack);
            });
        }).withResultSlot(5, 121, 35).build();
    }

    private List<ItemStack> getRelevantItems() {
        return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2), this.inputSlots.getItem(3), this.inputSlots.getItem(4));
    }

    private void shrinkStackInSlot(int pIndex) {
        ItemStack itemstack = this.inputSlots.getItem(pIndex);
        if (!itemstack.isEmpty()) {
            itemstack.shrink(1);
            this.inputSlots.setItem(pIndex, itemstack);
        }
    }

    @Override
    public int getSlotToQuickMoveTo(@NotNull ItemStack pStack) {
        return this.recipes.stream().map((smithingRecipe) -> {
            return findSlotMatchingIngredient(smithingRecipe, pStack);
        }).filter(Optional::isPresent).findFirst().orElse(Optional.of(List.of(0))).get().get(0);
    }

    private static Optional<List<Integer>> findSlotMatchingIngredient(SmithingRecipe pRecipe, ItemStack pStack) {
        if (pRecipe.isTemplateIngredient(pStack)) {
            return Optional.of(List.of(0));
        } else if (pRecipe.isBaseIngredient(pStack)) {
            return Optional.of(List.of(1));
        } else {
            return pRecipe.isAdditionIngredient(pStack) ? Optional.of(List.of(2, 3, 4)) : Optional.empty();
        }
    }

    @Override
    public boolean canTakeItemForPickAll(@NotNull ItemStack pStack, Slot pSlot) {
        return pSlot.container != this.resultSlots && super.canTakeItemForPickAll(pStack, pSlot);
    }

    @Override
    public boolean canMoveIntoInputSlots(@NotNull ItemStack pStack) {
        return this.recipes.stream().map((smithingRecipe) -> {
            return findSlotMatchingIngredient(smithingRecipe, pStack);
        }).anyMatch(Optional::isPresent);
    }

}
