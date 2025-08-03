package committee.nova.mods.avaritia.api.init.event;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * @author: cnlimiter
 */
public class RecipeManagerLoadingEvent extends Event {
    private final RecipeManager manager;
    private final List<Recipe<?>> recipes;

    public RecipeManagerLoadingEvent(RecipeManager manager, List<Recipe<?>> recipes) {
        this.manager = manager;
        this.recipes = recipes;
    }

    public RecipeManager getRecipeManager() {
        return this.manager;
    }

    public void addRecipe(Recipe<?> recipe) {
        this.recipes.add(recipe);
    }
}
