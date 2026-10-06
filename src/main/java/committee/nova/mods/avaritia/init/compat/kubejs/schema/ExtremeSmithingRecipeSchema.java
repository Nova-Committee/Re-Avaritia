package committee.nova.mods.avaritia.init.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/21 15:56
 * @Description:
 */
public interface ExtremeSmithingRecipeSchema {
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");
    RecipeKey<Ingredient> TEMPLATE = IngredientComponent.INGREDIENT.instance().inputKey("template");
    RecipeKey<Ingredient> BASE = IngredientComponent.INGREDIENT.instance().inputKey("base");
    RecipeKey<Ingredient> ADDITION = IngredientComponent.INGREDIENT.instance().inputKey("addition");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, TEMPLATE, BASE, ADDITION)
            .constructor(RESULT, TEMPLATE, BASE, ADDITION);
}
