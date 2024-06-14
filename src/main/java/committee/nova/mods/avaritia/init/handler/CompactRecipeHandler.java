package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedExtremeCraftingRecipe;
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
public class CompactRecipeHandler {
    @SubscribeEvent
    public static void onRegisterRecipes(RegisterRecipesEvent event) {


    }


    public static void addAE2Recipes(RegisterRecipesEvent event){
        if (Static.isLoad("ae2")){

        }
    }

}
