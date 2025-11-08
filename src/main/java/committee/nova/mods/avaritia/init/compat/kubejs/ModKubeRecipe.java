package committee.nova.mods.avaritia.init.compat.kubejs;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/18 22:16
 * @Description: Clase base para recetas personalizadas de Avaritia integradas con KubeJS.
 * KUBEJS 2101.7.2-build.309 COMPATIBILITY BY SrNadien.
 */
public abstract class ModKubeRecipe extends KubeRecipe {
    @HideFromJS
    public <T> T computeIfAbsent(RecipeKey<T> key, Supplier<T> supplier) {
        if (getValue(key) == null) {
            setValue(key, supplier.get());
        }
        return getValue(key);
    }

    /**
     * Cada receta concreta deberá definir su validación.
     */
    protected abstract void validate();

    /**
     * Ya no se usa @Override porque afterLoaded() fue eliminado de KubeRecipe.
     * La validación deberá ejecutarse manualmente o desde el evento AfterRecipesLoadedKubeEvent.
     */
    public void onAfterRecipesLoaded() {
        this.validate();
    }
}
