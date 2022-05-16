package nova.committee.avaritia.init.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nova.committee.avaritia.Static;
import nova.committee.avaritia.common.item.singularity.Singularity;
import nova.committee.avaritia.common.recipe.CompressorRecipe;
import nova.committee.avaritia.init.event.RegisterRecipesEvent;
import nova.committee.avaritia.util.SingularityUtils;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 20:34
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DynamicRecipeHandler {
    @SubscribeEvent
    public static void onRegisterRecipes(RegisterRecipesEvent event) {
        for (var singularity : SingularityRegistryHandler.getInstance().getSingularities()) {
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
        int timeRequired = 120; //TODO config

        return new CompressorRecipe(recipeId, ingredient, output, ingredientCount, timeRequired);
    }
}
