package committee.nova.mods.avaritia.api.init.event;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * @author cnlimiter
 */
public class RegisterRecipesEvent extends Event {
    private final ICondition.IContext context;
    private final RecipeManager manager;
    private final List<Recipe<?>> recipes;

    public RegisterRecipesEvent(RecipeManager manager, ICondition.IContext context, List<Recipe<?>> recipes) {
        this.manager = manager;
        this.context = context;
        this.recipes = recipes;
    }

    public ICondition.IContext getContext() {
        return this.context;
    }

    public RecipeManager getRecipeManager() {
        return this.manager;
    }

    public void addRecipe(Recipe<?> recipe) {
        this.recipes.add(recipe);
    }
}
