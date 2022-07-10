package nova.committee.avaritia.init.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import nova.committee.avaritia.common.crafting.recipe.CompressorRecipe;
import nova.committee.avaritia.init.registry.ModRecipeTypes;
import nova.committee.avaritia.util.RecipeUtil;
import org.openzen.zencode.java.ZenCodeType;

import java.util.HashMap;
import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/17 8:35
 * Version: 1.0
 */
@ZenCodeType.Name("mods.avaritia.CompressionCrafting")
@ZenRegister
public class CompressionCrafting {

    @ZenCodeType.Method
    public static void addRecipe(String id, IIngredient input, IItemStack output, int inputCount, int timecost) {
        CraftTweakerAPI.apply(new IRuntimeAction() {
            @Override
            public void apply() {
                CompressorRecipe recipe = new CompressorRecipe(new ResourceLocation("crafttweaker", id), input.asVanillaIngredient(), output.getInternal(), inputCount, timecost);
                RecipeUtil.addRecipe(recipe);
            }

            @Override
            public String describe() {
                return "Adding Compression Crafting recipe for " + output.getCommandString();
            }
        });
    }

    @ZenCodeType.Method
    public static void remove(IItemStack stack) {
        CraftTweakerAPI.apply(new IRuntimeAction() {
            @Override
            public void apply() {
                List<ResourceLocation> recipes = RecipeUtil.getRecipes()
                        .getOrDefault(ModRecipeTypes.RecipeTypes.COMPRESSOR, new HashMap<>())
                        .values().stream()
                        .filter(r -> r.getResultItem().sameItem(stack.getInternal()))
                        .map(Recipe::getId)
                        .toList();

                recipes.forEach(r -> {
                    RecipeUtil.getRecipes().get(ModRecipeTypes.RecipeTypes.COMPRESSOR).remove(r);
                });
            }

            @Override
            public String describe() {
                return "Removing Compression Crafting recipes for " + stack.getCommandString();
            }
        });
    }
}
