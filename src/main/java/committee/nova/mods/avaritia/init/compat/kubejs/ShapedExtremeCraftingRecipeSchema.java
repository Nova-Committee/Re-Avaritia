package committee.nova.mods.avaritia.init.compat.kubejs;


import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema;

import static dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema.INGREDIENTS;
import static dev.latvian.mods.kubejs.recipe.schema.minecraft.ShapedRecipeSchema.RESULT;

/**
 * Name: Avaritia-forge / ShapedExtremeCraftingRecipeSchema
 * Author: cnlimiter
 * CreateTime: 2023/11/13 22:52
 * Description:
 */

public interface ShapedExtremeCraftingRecipeSchema {
    RecipeSchema SCHEMA = new RecipeSchema(ShapedRecipeSchema.ShapedRecipeJS.class, ShapedRecipeSchema.ShapedRecipeJS::new, INGREDIENTS, RESULT);

}
