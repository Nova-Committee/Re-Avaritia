package committee.nova.mods.avaritia.common.crafting.recipe;

import committee.nova.mods.avaritia.api.common.crafting.ISpecialRecipe;
import committee.nova.mods.avaritia.api.common.crafting.ITierRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.jetbrains.annotations.NotNull;

/**
 * @Name: BaseTableCraftingRecipe
 * @author cnlimiter
 * @CreateTime: 2024/11/30 19:10
 * @Description:
 *
 **/
public interface ITierCraftingRecipe extends ISpecialRecipe, ITierRecipe {

    @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries);

    default boolean canCraftInDimensions(int width, int height) {
        return true;
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
    default @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
