package committee.nova.mods.avaritia.init.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

import static committee.nova.mods.avaritia.init.compat.kubejs.KjsUtils.optionalList;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/9 22:47
 * @Description:
 */
public interface ShapelessTableRecipeSchema{

    RecipeKey<List<Ingredient>> INGREDIENTS = optionalList(IngredientComponent.INGREDIENT, "ingredients", ComponentRole.INPUT);
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.inputKey("result").defaultOptional();
    RecipeKey<Integer> TIER = NumberComponent.INT.inputKey("tier").optional(0);

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, RESULT, TIER)
            .constructor(RESULT, INGREDIENTS)
            .constructor(RESULT, TIER, INGREDIENTS);
}
