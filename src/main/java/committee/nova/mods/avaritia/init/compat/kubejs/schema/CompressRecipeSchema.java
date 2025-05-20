package committee.nova.mods.avaritia.init.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;

/**
 * Author cnlimiter
 * CreateTime 2023/9/17 0:50
 * Name CompressRecipeSchema
 * Description
 */

public interface CompressRecipeSchema {
    RecipeKey<ItemStack> INGREDIENT = ItemStackComponent.ITEM_STACK.inputKey("ingredient");
    RecipeKey<ItemStack> OUTPUT = ItemStackComponent.ITEM_STACK.outputKey("result");
    RecipeKey<Integer> INPUT_COUNT = NumberComponent.INT.inputKey("inputCount").optional(1000);
    RecipeKey<Integer> TIME_COST = NumberComponent.INT.inputKey("timeCost").optional(240);
    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENT, OUTPUT, INPUT_COUNT, TIME_COST);
}
