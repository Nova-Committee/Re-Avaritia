package nova.committee.avaritia.init.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import nova.committee.avaritia.common.recipe.ShapedExtremeCraftingRecipe;
import nova.committee.avaritia.common.recipe.ShapelessExtremeCraftingRecipe;
import nova.committee.avaritia.init.registry.ModRecipeTypes;
import nova.committee.avaritia.util.RecipeUtil;
import org.openzen.zencode.java.ZenCodeType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/17 8:39
 * Version: 1.0
 */
@ZenCodeType.Name("mods.avaritia.ExtremeTableCrafting")
@ZenRegister
public class ExtremeTableCrafting {

    @ZenCodeType.Method
    public static void addShaped(String id, IItemStack output, IIngredient[][] inputs) {


        CraftTweakerAPI.apply(new IRuntimeAction() {
            @Override
            public void apply() {
                int height = inputs.length;
                int width = 0;
                for (var row : inputs) {
                    if (width < row.length) {
                        width = row.length;
                    }
                }

                var ingredients = NonNullList.withSize(height * width, Ingredient.EMPTY);
                Map<Integer, Function<ItemStack, ItemStack>> transformers = new HashMap<>();

                for (int a = 0; a < height; a++) {
                    for (int b = 0; b < inputs[a].length; b++) {
                        var iing = inputs[a][b];
                        var ing = iing.asVanillaIngredient();
                        int i = a * width + b;
                        ingredients.set(i, ing);

                        if (ing != Ingredient.EMPTY) {
                            transformers.put(i, stack -> {
                                var istack = iing.getRemainingItem(new MCItemStack(stack));
                                return istack.getInternal();
                            });
                        }
                    }
                }

                var recipe = new ShapedExtremeCraftingRecipe(new ResourceLocation("crafttweaker", id), width, height, ingredients, output.getInternal());

                recipe.setTransformers(transformers);

                RecipeUtil.addRecipe(recipe);
            }

            @Override
            public String describe() {
                return "Adding Shaped Extreme Table Crafting recipe for " + output.getCommandString();
            }
        });
    }

    @ZenCodeType.Method
    public static void addShapeless(String id, IItemStack output, IIngredient[] inputs) {

        CraftTweakerAPI.apply(new IRuntimeAction() {
            @Override
            public void apply() {
                Map<Integer, Function<ItemStack, ItemStack>> transformers = new HashMap<>();

                for (int i = 0; i < inputs.length; i++) {
                    var iing = inputs[i];
                    var ing = iing.asVanillaIngredient();

                    if (ing != Ingredient.EMPTY) {
                        transformers.put(i, stack -> {
                            var istack = iing.getRemainingItem(new MCItemStack(stack));
                            return istack.getInternal();
                        });
                    }
                }

                var recipe = new ShapelessExtremeCraftingRecipe(new ResourceLocation("crafttweaker", id), toIngredientsList(inputs), output.getInternal());

                recipe.setTransformers(transformers);

                RecipeUtil.addRecipe(recipe);
            }

            @Override
            public String describe() {
                return "Adding Shapeless Extreme Table Crafting recipe for " + output.getCommandString();
            }
        });
    }

    @ZenCodeType.Method
    public static void remove(IItemStack stack) {
        CraftTweakerAPI.apply(new IRuntimeAction() {
            @Override
            public void apply() {
                var recipes = RecipeUtil.getRecipes()
                        .getOrDefault(ModRecipeTypes.RecipeTypes.CRAFTING, new HashMap<>())
                        .values().stream()
                        .filter(r -> r.getResultItem().sameItem(stack.getInternal()))
                        .map(Recipe::getId)
                        .toList();

                recipes.forEach(r -> {
                    RecipeUtil.getRecipes().get(ModRecipeTypes.RecipeTypes.CRAFTING).remove(r);
                });
            }

            @Override
            public String describe() {
                return "Removing Extreme Table Crafting recipes for " + stack.getCommandString();
            }
        });
    }

    private static NonNullList<Ingredient> toIngredientsList(IIngredient... ingredients) {
        return Arrays.stream(ingredients)
                .map(IIngredient::asVanillaIngredient)
                .collect(Collectors.toCollection(NonNullList::create));
    }

}
