package nova.committee.avaritia.init.event;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.eventbus.api.Event;
import nova.committee.avaritia.util.RecipeUtil;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 20:37
 * Version: 1.0
 */
public class RegisterRecipesEvent extends Event {
    private final RecipeManager manager;

    public RegisterRecipesEvent(RecipeManager manager) {
        this.manager = manager;
    }

    public RecipeManager getRecipeManager() {
        return this.manager;
    }

    public void register(Recipe<?> recipe) {
        RecipeUtil.addRecipe(recipe);
    }
}
