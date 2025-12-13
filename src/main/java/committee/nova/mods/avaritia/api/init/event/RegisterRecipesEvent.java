package committee.nova.mods.avaritia.api.init.event;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author cnlimiter
 */
public class RegisterRecipesEvent extends Event {
    private final RecipeManager manager;
    private final CopyOnWriteArrayList<Recipe<?>> recipes;

    public RegisterRecipesEvent(RecipeManager manager, CopyOnWriteArrayList<Recipe<?>> recipes) {
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
