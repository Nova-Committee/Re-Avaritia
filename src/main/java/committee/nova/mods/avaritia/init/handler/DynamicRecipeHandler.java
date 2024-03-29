package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.util.RecipeUtil;
import committee.nova.mods.avaritia.util.SingularityUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 20:34
 * Version: 1.0
 */
@Mod.EventBusSubscriber
public class DynamicRecipeHandler {
    @SubscribeEvent
    public static void onRegisterRecipes(RegisterRecipesEvent event) {

//        var infinity_catalyst = (InfinityCatalystCraftRecipe) RecipeUtil.getRecipe(Static.rl("infinity_catalyst"));
//        SingularityRegistryHandler.getInstance().getSingularities()
//                .stream()
//                .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
//                .limit(81)
//                .map(SingularityUtil::getItemForSingularity)
//                .map(Ingredient::of)
//                .forEach(ingredient -> {
//                    infinity_catalyst.ingredients.add(ingredient);
//                });
//        event.register(new RecipeHolder<>(Static.rl("infinity_catalyst"), new InfinityCatalystCraftRecipe("catalyst", infinity_catalyst.ingredients)));

        for (var singularity : SingularityRegistryHandler.getInstance().getSingularities()) {
            if (singularity.isRecipeDisabled()) {
                continue;
            }

            var compressorRecipe = makeSingularityRecipe(singularity);

            if (compressorRecipe != null)
                event.register(compressorRecipe);
        }
    }


    private static RecipeHolder<CompressorRecipe> makeSingularityRecipe(Singularity singularity) {
        var ingredient = singularity.getIngredient();
        if (ingredient == Ingredient.EMPTY)
            return null;

        var id = singularity.getId();
        var recipeId = new ResourceLocation(Static.MOD_ID, id.getPath() + "_singularity");
        var output = SingularityUtil.getItemForSingularity(singularity);
        int ingredientCount = singularity.getIngredientCount();
        int timeRequired = singularity.getTimeRequired();

        return new RecipeHolder<>(recipeId, new CompressorRecipe("singularity", ingredient, output, ingredientCount, timeRequired));
    }

}
