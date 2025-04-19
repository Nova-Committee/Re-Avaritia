package committee.nova.mods.avaritia.api.init.event;

import committee.nova.mods.avaritia.api.Lib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.Event;

import java.util.HashMap;
import java.util.Map;

public class RegisterRecipesEvent extends Event {
    private final RecipeManager recipeManager;

    public RegisterRecipesEvent(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    public RecipeManager getRecipeManager() {
        if (!(recipeManager.byType instanceof HashMap)) {
            try {
                Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes = new HashMap<>();
                recipeManager.byType.forEach((type, map) ->
                    newRecipes.put(type, new HashMap<>(map))
                );
                recipeManager.byType = newRecipes;
            } catch (Exception e) {
                Lib.LOGGER.error("Failed to convert recipes: {}", e.getMessage());
            }
        }
        if (!(recipeManager.byName instanceof HashMap)) {
            try {
                recipeManager.byName = new HashMap<>(recipeManager.byName);
            } catch (Exception e) {
                Lib.LOGGER.error("Failed to convert byName: {}", e.getMessage());
            }
        }
        return recipeManager;
    }

    public void addRecipe(Recipe<?> recipe) {
        RecipeManager manager = getRecipeManager();

        try {
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes = new HashMap<>(manager.byType);
            newRecipes.computeIfAbsent(recipe.getType(), t -> new HashMap<>())
                      .put(recipe.getId(), recipe);

            Map<ResourceLocation, Recipe<?>> newByName = new HashMap<>(manager.byName);
            newByName.put(recipe.getId(), recipe);

            manager.byType = newRecipes;
            manager.byName = newByName;
        } catch (Exception e) {
            Lib.LOGGER.error("Failed to add recipe: {}", e.getMessage());
        }
    }

    public <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> getRecipes(RecipeType<T> type) {
        return getRecipeManager().byType(type);
    }

    public Recipe<?> getRecipe(ResourceLocation name) {
        return getRecipeManager().byName.get(name);
    }
}
    
