package committee.nova.mods.avaritia.init.event;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 20:37
 * Version: 1.0
 */
public final class RegisterRecipesEvent extends Event {
    private final RecipeManager recipeManager;

    public RegisterRecipesEvent(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    public RecipeManager getRecipeManager() {
        if (recipeManager.recipes instanceof ImmutableMap) {
            recipeManager.recipes = new HashMap<>(recipeManager.recipes);
            recipeManager.recipes.replaceAll((t, v) -> new HashMap<>(recipeManager.recipes.get(t)));
        }

        if (recipeManager.byName instanceof ImmutableMap) {
            recipeManager.byName = new HashMap<>(recipeManager.byName);
        }

        return recipeManager;
    }

    public void addRecipe(Recipe<?> recipe) {
        addRecipe(getRecipeManager(), recipe);
    }

    public void addRecipe(RecipeManager recipeManager, Recipe<?> recipe) {
        recipeManager.recipes.computeIfAbsent(recipe.getType(), (t) -> new HashMap<>()).put(recipe.getId(), recipe);
        recipeManager.byName.put(recipe.getId(), recipe);
    }

    public <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> getRecipes(RecipeType<T> type) {
        return getRecipeManager().byType(type);
    }

    public Recipe<?> getRecipe(ResourceLocation name) {
        return getRecipeManager().byName.get(name);
    }
}
