package committee.nova.mods.avaritia.api.init.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * RegisterRecipeCallback
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/5/24 上午12:09
 */
public interface RegisterRecipeCallback {
    Event<RegisterRecipeCallback> EVENT = EventFactory.createArrayBacked(RegisterRecipeCallback.class, callbacks -> ((recipeManager) -> {
        for (RegisterRecipeCallback event : callbacks)
            event.register(recipeManager);
    }));

    void register(RecipeManager recipeManager);
}
