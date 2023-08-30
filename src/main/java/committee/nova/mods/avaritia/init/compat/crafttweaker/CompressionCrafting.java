package committee.nova.mods.avaritia.init.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.util.recipes.RecipeUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.server.ServerLifecycleHooks;
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
                var access = ServerLifecycleHooks.getCurrentServer().registryAccess();
                List<ResourceLocation> recipes = RecipeUtil.getRecipes()
                        .getOrDefault(ModRecipeTypes.COMPRESSOR_RECIPE.get(), new HashMap<>())
                        .values().stream()
                        .filter(r -> r.getResultItem().is(stack.getInternal().getItem()))
                        .map(Recipe::getId)
                        .toList();

                recipes.forEach(r -> {
                    RecipeUtil.getRecipes().get(ModRecipeTypes.COMPRESSOR_RECIPE.get()).remove(r);
                });
            }

            @Override
            public String describe() {
                return "Removing Compression Crafting recipes for " + stack.getCommandString();
            }

        });
    }
}
