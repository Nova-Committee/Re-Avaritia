package committee.nova.mods.avaritia.init.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/21 15:56
 * @Description:
 */
public interface ExtremeSmithingRecipeSchema {
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");
    RecipeKey<ItemStack> TEMPLATE = ItemStackComponent.ITEM_STACK.inputKey("template");
    RecipeKey<ItemStack> BASE = ItemStackComponent.ITEM_STACK.inputKey("base");
    RecipeKey<ItemStack> ADDITION = ItemStackComponent.ITEM_STACK.inputKey("addition");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, TEMPLATE, BASE, ADDITION)
            .constructor(RESULT, TEMPLATE, BASE, ADDITION);
}
