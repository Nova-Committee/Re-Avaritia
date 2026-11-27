package committee.nova.mods.avaritia.init.compat.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.util.IntBounds;

import java.util.List;

/**
 * @author cnlimiter
 */
public class KjsUtils {
    public static <T> RecipeKey<List<T>> optionalList(RecipeComponentType<T> component, String name, ComponentRole role) {
        return component.instance()
                .asConditionalList()
                .orSelf()
                .withBounds(IntBounds.OPTIONAL)
                .key(name, role)
                .optional(List.of());
    }
}
