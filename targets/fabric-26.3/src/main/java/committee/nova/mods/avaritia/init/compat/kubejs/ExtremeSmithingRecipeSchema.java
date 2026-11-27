package committee.nova.mods.avaritia.init.compat.kubejs;

import committee.nova.mods.avaritia.init.registry.ModItems;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/12/21 15:56
 * @Description:
 */
public interface ExtremeSmithingRecipeSchema {
    RecipeKey<OutputItem> RESULT = ItemComponents.OUTPUT.key("result");
    RecipeKey<InputItem> TEMPLATE = ItemComponents.INPUT.key("template");
    RecipeKey<InputItem> BASE = ItemComponents.INPUT.key("base");
    RecipeKey<InputItem> ADDITION = ItemComponents.INPUT.key("addition");

    RecipeSchema SCHEMA = new RecipeSchema(RESULT, TEMPLATE, BASE, ADDITION)
            .uniqueOutputId(RESULT)
            .constructor(RESULT, TEMPLATE, BASE, ADDITION)
            .constructor(RecipeConstructor.Factory.defaultWith((recipe, key) -> {
                if (key == TEMPLATE) {
                    return InputItem.of(Ingredient.of(ModItems.upgrade_smithing_template.get()), 1);
                } else {
                    return null;
                }
            }), RESULT, BASE, ADDITION);
}
