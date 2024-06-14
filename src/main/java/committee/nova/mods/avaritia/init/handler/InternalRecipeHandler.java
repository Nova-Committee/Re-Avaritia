package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.event.RegisterRecipesEvent;
import committee.nova.mods.avaritia.util.RecipeUtil;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 20:34
 * Version: 1.0
 */
@Mod.EventBusSubscriber
public class InternalRecipeHandler {
    @SubscribeEvent
    public static void onRegisterRecipes(RegisterRecipesEvent event) {
        List<Singularity> allSingularities = SingularityRegistryHandler.getInstance().getSingularities();

        var infinity_catalyst = (InfinityCatalystCraftRecipe) RecipeUtil.getRecipe(Static.rl("infinity_catalyst"));

        if (infinity_catalyst.getGroup().equals("default")){
            allSingularities.stream()
                    .filter(singularity -> singularity.getIngredient() != Ingredient.EMPTY)
                    .limit(81)
                    .map(SingularityUtils::getItemForSingularity)
                    .map(Ingredient::of)
                    .forEach(infinity_catalyst.inputs::add);

            event.register(new InfinityCatalystCraftRecipe(Static.rl("infinity_catalyst"), "default", infinity_catalyst.inputs));
        }

        for (var singularity : SingularityRegistryHandler.getInstance().getSingularities()) {
            if (singularity.isRecipeDisabled()) {
                continue;
            }

            var compressorRecipe = makeSingularityRecipe(singularity);

            if (compressorRecipe != null)
                event.register(compressorRecipe);
        }
    }


    private static CompressorRecipe makeSingularityRecipe(Singularity singularity) {
        var ingredient = singularity.getIngredient();
        if (ingredient == Ingredient.EMPTY)
            return null;

        var id = singularity.getId();
        var recipeId = new ResourceLocation(Static.MOD_ID, id.getPath() + "_singularity");
        var output = SingularityUtils.getItemForSingularity(singularity);
        int ingredientCount = singularity.getIngredientCount();
        int timeRequired = singularity.getTimeRequired();

        return new CompressorRecipe(recipeId, ingredient, output, ingredientCount, timeRequired);
    }

}
