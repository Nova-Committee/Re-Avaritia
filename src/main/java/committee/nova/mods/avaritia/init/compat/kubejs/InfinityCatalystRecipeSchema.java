package committee.nova.mods.avaritia.init.compat.kubejs;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;

/**
 * Author cnlimiter
 * CreateTime 2023/9/17 0:50
 * Name CompressRecipeSchema
 * Description
 */

public interface InfinityCatalystRecipeSchema {
    RecipeKey<String> GROUP = StringComponent.NON_EMPTY.otherKey("group").optional("default");
    RecipeKey<ItemStack[]> INGREDIENTS = ItemStackComponent.ITEM_STACK.inputKey("ingredients");
    RecipeSchema SCHEMA = new RecipeSchema(RecipeJS.class, RecipeJS::new, GROUP, INGREDIENTS);
}
