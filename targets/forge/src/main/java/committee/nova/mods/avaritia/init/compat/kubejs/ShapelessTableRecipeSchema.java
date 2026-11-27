package committee.nova.mods.avaritia.init.compat.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapelessRecipeSchema;

import static dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema.INGREDIENTS;
import static dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema.RESULT;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/11/9 22:47
 * @Description:
 */
public interface ShapelessTableRecipeSchema {
    RecipeKey<Integer> TIER = NumberComponent.INT.key("tier").optional(0);
    RecipeSchema SCHEMA = new RecipeSchema(ShapelessRecipeSchema.ShapelessRecipeJS.class, ShapelessRecipeSchema.ShapelessRecipeJS::new, TIER, RESULT, INGREDIENTS)
            .uniqueOutputId(RESULT);
}
