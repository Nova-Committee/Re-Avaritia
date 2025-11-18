package committee.nova.mods.avaritia.init.compat.kubejs;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/18 22:16
  * @Description:
 */
public abstract class ModKubeRecipe extends KubeRecipe {
    @HideFromJS
    public <T> T computeIfAbsent(RecipeKey<T> key, Supplier<T> supplier) {
        if (getValue(key) == null) {
            setValue(key, supplier.get());
        }
        return getValue(key);
    }

  
    protected abstract void validate();

    
    public void onAfterRecipesLoaded() {
        this.validate();
    }
}
