package com.avaritia.api.common.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICompressorRecipe extends Recipe<CraftingInput> {
    @NotNull Ingredient getInput();

    int getInputCount();

    int getTimeCost();

    @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries);

    @NotNull NonNullList<Ingredient> getIngredients();

    default boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    default @NotNull ItemStack assemble(@NotNull CraftingInput input) {
        return this.getResultItem(null).copy();
    }

    @Override
    default boolean showNotification() {
        return true;
    }

    @Override
    default @NotNull String group() {
        return "";
    }

    @Override
    default @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(this.getIngredients());
    }

    @Override
    default @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    default @NotNull List<RecipeDisplay> display() {
        ItemStack result = this.getResultItem(null);
        if (result.isEmpty()) {
            return List.of();
        }
        return List.of(new ShapelessCraftingRecipeDisplay(
                this.getIngredients().stream().map(Ingredient::display).toList(),
                new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(result)),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
        ));
    }
}
