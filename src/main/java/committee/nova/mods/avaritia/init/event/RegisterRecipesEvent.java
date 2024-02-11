package committee.nova.mods.avaritia.init.event;

import committee.nova.mods.avaritia.util.RecipeUtil;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.Event;

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

    public void register(RecipeHolder<?> recipe) {
        RecipeUtil.addRecipe(recipe);
    }
}
