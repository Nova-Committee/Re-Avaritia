package committee.nova.mods.avaritia.api.init.event;

import committee.nova.mods.avaritia.util.RecipeUtil;
import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * RegisterRecipeCallback
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午12:09
 */
public class RegisterRecipesEvent extends BaseEvent {
    public static  Event<RegisterRecipesEvent.Register> EVENT = EventFactory.createArrayBacked(RegisterRecipesEvent.Register.class, callbacks -> ((recipeManager) -> {
        for (RegisterRecipesEvent.Register event : callbacks)
            event.post(recipeManager);
    }));

    public interface Register {
        void post(RegisterRecipesEvent event);
    }


    private final RecipeManager manager;

    public RegisterRecipesEvent(RecipeManager manager) {
        this.manager = manager;
    }

    public RecipeManager getRecipeManager() {
        return this.manager;
    }

    public void register(Recipe<?> recipe) {
        RecipeUtil.addRecipe(manager, recipe);
    }

    @Override
    public void sendEvent() {
        EVENT.invoker().post(this);
    }
}
