package committee.nova.mods.avaritia.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

import static committee.nova.mods.avaritia.compat.kubejs.KjsUtils.COMPAT_INGREDIENT;
import static committee.nova.mods.avaritia.compat.kubejs.KjsUtils.COMPAT_ITEM_STACK;
import static committee.nova.mods.avaritia.compat.kubejs.KjsUtils.optionalList;

public interface ShapelessTableRecipeSchema {
    RecipeKey<List<Ingredient>> INGREDIENTS =
            optionalList(COMPAT_INGREDIENT, "ingredients", ComponentRole.INPUT);
    RecipeKey<ItemStack> RESULT = COMPAT_ITEM_STACK.inputKey("result").defaultOptional();
    RecipeKey<Integer> TIER = NumberComponent.INT.inputKey("tier").optional(0);

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, RESULT, TIER)
            .constructor(RESULT, INGREDIENTS)
            .constructor(RESULT, TIER, INGREDIENTS);
}
