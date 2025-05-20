package committee.nova.mods.avaritia.init.compat.kubejs.schema;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.compat.kubejs.ModKubeRecipe;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/11/9 22:47
 * @Description:
 */
public interface ShapelessTableRecipeSchema{
    @SuppressWarnings({"DataFlowIssue", "unused"})
    class ShapelessTableKubeRecipe extends ModKubeRecipe {
        public ShapelessTableKubeRecipe requires(Ingredient... ingredient) {
            computeIfAbsent(INGREDIENTS, ArrayList::new).addAll(Arrays.stream(ingredient).toList());
            save();
            return this;
        }

        public ShapelessTableKubeRecipe requires(Ingredient ingredient, int count) {
            if (getValue(INGREDIENTS) == null) setValue(INGREDIENTS, new ArrayList<>());
            for (int i = 0; i < count; i++) {
                getValue(INGREDIENTS).add(ingredient);
            }
            save();
            return this;
        }

        public ShapelessTableKubeRecipe result(ItemStack stack) {
            if (getValue(RESULT) == null) setValue(RESULT, new ArrayList<>());
            getValue(RESULT).add(stack);
            save();
            return this;
        }

        public ShapelessTableKubeRecipe tier(int tier) {
            setValue(TIER, tier);
            save();
            return this;
        }

        @Override
        protected void validate() {
            if (computeIfAbsent(INGREDIENTS, ArrayList::new).isEmpty()) {
                throw new KubeRuntimeException("Ingredients is Empty!").source(sourceLine);
            }
            if (computeIfAbsent(RESULT, ArrayList::new).isEmpty()) {
                throw new KubeRuntimeException("Ingredients is Empty!").source(sourceLine);
            }
        }
    }

    RecipeKey<List<Ingredient>> INGREDIENTS = IngredientComponent.INGREDIENT.instance().asList().inputKey("ingredients").defaultOptional();
    RecipeKey<List<ItemStack>> RESULT = ItemStackComponent.STRICT_ITEM_STACK.instance().asList().inputKey("result").defaultOptional();
    RecipeKey<Integer> TIER = NumberComponent.INT.inputKey("tier").optional(0);

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, RESULT, TIER)
            .factory(new KubeRecipeFactory(Const.rl("shapeless_table"), ShapelessTableKubeRecipe.class, ShapelessTableKubeRecipe::new))
            .constructor(INGREDIENTS, RESULT)
            .constructor(INGREDIENTS, RESULT, TIER)
            .constructor()
            ;
}
