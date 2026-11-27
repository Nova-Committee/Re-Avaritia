package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
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

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/10/12 23:00
 * @Description:
 */
public interface ITierCraftingRecipe extends Recipe<TierInput> {

    public int getTier();

    public boolean hasRequiredTier();

    @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries);

    @NotNull NonNullList<Ingredient> getIngredients();

    default boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    default @NotNull String getGroup() {
        return "";
    }

    @Override
    default @NotNull String group() {
        return this.getGroup();
    }

    @Override
    default @NotNull ItemStack assemble(@NotNull TierInput input) {
        return this.getResultItem(null).copy();
    }

    default @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull TierInput inventory) {
        return CraftingRecipe.defaultCraftingReminder(inventory);
    }

    @Override
    default boolean showNotification() {
        return true;
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
