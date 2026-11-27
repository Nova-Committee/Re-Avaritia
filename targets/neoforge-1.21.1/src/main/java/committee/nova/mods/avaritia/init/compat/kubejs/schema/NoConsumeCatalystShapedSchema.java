package committee.nova.mods.avaritia.init.compat.kubejs.schema;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.IntBounds;
import dev.latvian.mods.kubejs.util.TinyMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

import static committee.nova.mods.avaritia.init.compat.kubejs.KjsUtils.optionalList;

/**
 * @author cnlimiter
 */
public interface NoConsumeCatalystShapedSchema {
    RecipeKey<List<String>> PATTERN = optionalList(StringComponent.OPTIONAL_STRING, "pattern", ComponentRole.INPUT);
    RecipeKey<TinyMap<Character, Ingredient>> KEY = MapRecipeComponent.patternOf(IngredientComponent.INGREDIENT.instance(), IntBounds.DEFAULT).inputKey("key");
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");
    RecipeKey<Integer> TIER = NumberComponent.INT.inputKey("tier").optional(0);

    RecipeSchema SCHEMA = new RecipeSchema(PATTERN, KEY, RESULT, TIER)
            .constructor(PATTERN, KEY, RESULT, TIER);
}
