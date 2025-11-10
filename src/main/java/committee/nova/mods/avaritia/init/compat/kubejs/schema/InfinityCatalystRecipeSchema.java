package committee.nova.mods.avaritia.init.compat.kubejs.schema;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.compat.kubejs.ModKubeRecipe;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface InfinityCatalystRecipeSchema {

   
    @SuppressWarnings({"DataFlowIssue", "unused"})
    class InfinityCatalystKubeRecipe extends ModKubeRecipe {

        public InfinityCatalystKubeRecipe requires(Ingredient... ingredients) {
            computeIfAbsent(INGREDIENTS, ArrayList::new).addAll(Arrays.asList(ingredients));
            save();
            return this;
        }

        public InfinityCatalystKubeRecipe requires(Ingredient ingredient, int count) {
            if (getValue(INGREDIENTS) == null)
                setValue(INGREDIENTS, new ArrayList<>());
            for (int i = 0; i < count; i++) {
                getValue(INGREDIENTS).add(ingredient);
            }
            save();
            return this;
        }

        @Override
        protected void validate() {
            if (computeIfAbsent(INGREDIENTS, ArrayList::new).isEmpty()) {
                throw new KubeRuntimeException("Ingredients list is empty!").source(sourceLine);
            }
        }
    }

    
    RecipeKey<String> GROUP = StringComponent.STRING
            .key("group", ComponentRole.INPUT)
            .optional("default");

    RecipeKey<List<Ingredient>> INGREDIENTS = IngredientComponent.INGREDIENT
            .instance()
            .asList()
            .key("ingredients", ComponentRole.INPUT)
            .defaultOptional();

    
    RecipeSchema SCHEMA = new RecipeSchema(GROUP, INGREDIENTS)
            .factory(new KubeRecipeFactory(
                    Const.rl("infinity_catalyst"),
                    InfinityCatalystKubeRecipe.class,
                    InfinityCatalystKubeRecipe::new))
            .constructor(GROUP, INGREDIENTS);
}
