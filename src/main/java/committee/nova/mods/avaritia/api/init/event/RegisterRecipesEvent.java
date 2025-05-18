package committee.nova.mods.avaritia.api.init.event;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.Event;

import java.util.List;

public class RegisterRecipesEvent extends Event {
    private final RecipeManager manager;
    private final List<RecipeHolder<?>> recipes;

    public RegisterRecipesEvent(RecipeManager manager, List<RecipeHolder<?>> recipes) {
        this.manager = manager;
        this.recipes = recipes;
    }

    public RecipeManager getRecipeManager() {
        return this.manager;
    }

    public void addRecipe(RecipeHolder<?> recipe) {
        this.recipes.add(recipe);
    }
}
    
