package committee.nova.mods.avaritia.init.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedExtremeCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedExtremePattern;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessExtremeCraftingRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.util.RecipeUtil;
import committee.nova.mods.avaritia.util.math.NoNullList;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.openzen.zencode.java.ZenCodeType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
                ShapedExtremePattern pattern = new ShapedExtremePattern(width, height, ingredients, Optional.empty());

                var recipe = new ShapedExtremeCraftingRecipe("extreme", pattern, output.getInternal());

                recipe.setTransformers(transformers);

                RecipeUtil.addRecipe(new RecipeHolder<>(new ResourceLocation("crafttweaker", id), recipe));
            }

            @Override
            public String describe() {
                return "Adding Shaped Extreme Table Crafting recipe for " + output.getCommandString();
            }

            @Override
            public String systemName() {
                return "Adding Shaped recipe";
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

                var recipe = new ShapelessExtremeCraftingRecipe("extreme", toIngredientsNonNullList(inputs), output.getInternal());

                recipe.setTransformers(transformers);

                RecipeUtil.addRecipe(new RecipeHolder<>(new ResourceLocation("crafttweaker", id), recipe));
            }

            @Override
            public String describe() {
                return "Adding Shapeless Extreme Table Crafting recipe for " + output.getCommandString();
            }

            @Override
            public String systemName() {
                return "Adding Shapeless recipe";
            }

        });
    }

    @ZenCodeType.Method
    public static void addCatalyst(String id, IIngredient[] inputs) {

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

                var recipe = new InfinityCatalystCraftRecipe("catalyst", toIngredientsNonNullList(inputs));

                recipe.setTransformers(transformers);

                RecipeUtil.addRecipe(new RecipeHolder<>(new ResourceLocation("crafttweaker", id), recipe));
            }

            @Override
            public String describe() {
                return "Adding Shapeless Extreme Table Crafting recipe for Infinity Catalyst";
            }

            @Override
            public String systemName() {
                return "Adding Shapeless recipe for Infinity Catalyst";
            }

        });
    }

    @ZenCodeType.Method
    public static void remove(IItemStack stack) {
        CraftTweakerAPI.apply(new IRuntimeAction() {
            @Override
            public void apply() {
                var access = ServerLifecycleHooks.getCurrentServer().registryAccess();
                var recipes = RecipeUtil.getRecipes()
                        .getOrDefault(ModRecipeTypes.EXTREME_CRAFT_RECIPE.get(), new HashMap<>())
                        .values().stream()
                        .filter(r -> r.value().getResultItem(access).is(stack.getInternal().getItem()))
                        .map(RecipeHolder::id   )
                        .toList();

                recipes.forEach(r -> {
                    RecipeUtil.getRecipes().get(ModRecipeTypes.EXTREME_CRAFT_RECIPE.get()).remove(r);
                });
            }

            @Override
            public String describe() {
                return "Removing Extreme Table Crafting recipes for " + stack.getCommandString();
            }

            @Override
            public String systemName() {
                return "Removing recipes";
            }

        });
    }

    private static NoNullList<Ingredient> toIngredientsNoNullList(IIngredient... ingredients) {
        return Arrays.stream(ingredients)
                .map(IIngredient::asVanillaIngredient)
                .collect(Collectors.toCollection(NoNullList::create));
    }

    private static NonNullList<Ingredient> toIngredientsNonNullList(IIngredient... ingredients) {
        return Arrays.stream(ingredients)
                .map(IIngredient::asVanillaIngredient)
                .collect(Collectors.toCollection(NonNullList::create));
    }
}
