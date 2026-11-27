package committee.nova.mods.avaritia.api.init.event;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author cnlimiter
 */
public class RegisterRecipesEvent {
    private static final CopyOnWriteArrayList<Consumer<RegisterRecipesEvent>> LISTENERS = new CopyOnWriteArrayList<>();
    private final RecipeManager manager;
    private final CopyOnWriteArrayList<Recipe<?>> recipes;

    public RegisterRecipesEvent(RecipeManager manager, CopyOnWriteArrayList<Recipe<?>> recipes) {
        this.manager = manager;
        this.recipes = recipes;
    }

    public static void register(Consumer<RegisterRecipesEvent> listener) {
        LISTENERS.add(java.util.Objects.requireNonNull(listener));
    }

    public static void post(RegisterRecipesEvent event) {
        for (Consumer<RegisterRecipesEvent> listener : LISTENERS) {
            listener.accept(event);
        }
    }

    public RecipeManager getRecipeManager() {
        return this.manager;
    }

    public void addRecipe(Recipe<?> recipe) {
        this.recipes.add(recipe);
    }
}
