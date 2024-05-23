package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.init.event.RegisterRecipeCallback;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.util.RecipeUtil;
import committee.nova.mods.avaritia.util.SingularityUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 20:34
 * Version: 1.0
 */
public class DynamicRecipeHandler {

    public static void init() {
        RegisterRecipeCallback.EVENT.register(recipeManager -> {
            var infinity_catalyst = (InfinityCatalystCraftRecipe) RecipeUtil.getRecipe(Static.rl("infinity_catalyst"));
            SingularityRegistryHandler.getInstance().getSingularities()
                    .stream()
                    .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
                    .limit(81)
                    .map(SingularityUtil::getItemForSingularity)
                    .map(Ingredient::of)
                    .forEach(infinity_catalyst.inputs::add);
            register(new InfinityCatalystCraftRecipe(Static.rl("infinity_catalyst"), infinity_catalyst.inputs));

            for (var singularity : SingularityRegistryHandler.getInstance().getSingularities()) {
                if (singularity.isRecipeDisabled()) {
                    continue;
                }

                var compressorRecipe = makeSingularityRecipe(singularity);

                if (compressorRecipe != null)
                    register(compressorRecipe);
            }
        });


    }

    private static void register(Recipe<?> recipe) {
        RecipeUtil.addRecipe(recipe);
    }

    private static CompressorRecipe makeSingularityRecipe(Singularity singularity) {
        var ingredient = singularity.getIngredient();
        if (ingredient == Ingredient.EMPTY)
            return null;

        var id = singularity.getId();
        var recipeId = new ResourceLocation(Static.MOD_ID, id.getPath() + "_singularity");
        var output = SingularityUtil.getItemForSingularity(singularity);
        int ingredientCount = singularity.getIngredientCount();
        int timeRequired = singularity.getTimeRequired();

        return new CompressorRecipe(recipeId, ingredient, output, ingredientCount, timeRequired);
    }

}
