package committee.nova.mods.avaritia.api.common.inventory;

import lombok.Setter;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/14 19:29
 * @Description:
 */
public class CachedRecipe <I extends RecipeInput, T extends Recipe<I>> {
    private final RecipeType<T> type;
    @Setter private T recipe;

    public CachedRecipe(RecipeType<T> type) {
        this.type = type;
    }

    public boolean check(I inventory, Level level) {
        if (this.recipe != null && this.recipe.matches(inventory, level)) {
            return true;
        } else {
            this.recipe = level.getRecipeManager().getRecipeFor(this.type, inventory, level).map(RecipeHolder::value).orElse(null);
            return this.recipe != null;
        }
    }

    public boolean exists() {
        return this.recipe != null;
    }

    public T get() {
        return this.recipe;
    }

    public T checkAndGet(I inventory, Level level) {
        return this.check(inventory, level) ? this.recipe : null;
    }
}
