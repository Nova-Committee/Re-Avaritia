package committee.nova.mods.avaritia.init.compat.kubejs;

import committee.nova.mods.avaritia.init.registry.ModItems;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
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
    RecipeKey<ItemStack> TEMPLATE = ItemStackComponent.ITEM_STACK.inputKey("template");
    RecipeKey<ItemStack> BASE = ItemStackComponent.ITEM_STACK.inputKey("base");
    RecipeKey<ItemStack> ADDITION = ItemStackComponent.ITEM_STACK.inputKey("addition");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, TEMPLATE, BASE, ADDITION)
            .uniqueId(RESULT)
            .constructor(RESULT, TEMPLATE, BASE, ADDITION)
            .constructor(RecipeConstructor.Factory.defaultWith((recipe, key) -> {
                if (key == TEMPLATE) {
                    return InputItem.of(Ingredient.of(ModItems.upgrade_smithing_template.get()), 1);
                } else {
                    return null;
                }
            }), RESULT, BASE, ADDITION);
}
