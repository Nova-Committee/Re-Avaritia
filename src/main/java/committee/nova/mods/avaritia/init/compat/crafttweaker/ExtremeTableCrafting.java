package committee.nova.mods.avaritia.init.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.action.recipe.ActionRemoveRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import committee.nova.mods.avaritia.api.common.crafting.ISpecialRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedExtremeCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessExtremeCraftingRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.util.RecipeUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.openzen.zencode.java.ZenCodeType;

import java.util.*;
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
public class ExtremeTableCrafting implements IRecipeManager<ISpecialRecipe> {
    private static final ExtremeTableCrafting INSTANCE = new ExtremeTableCrafting();
    @Override
    public RecipeType<ISpecialRecipe> getRecipeType() {
        return ModRecipeTypes.EXTREME_CRAFT_RECIPE.get();
    }
    @ZenCodeType.Method
    public static void addShaped(String name, IItemStack output, IIngredient[][] inputs) {
        var id = CraftTweakerConstants.rl(INSTANCE.fixRecipeName(name));
        int height = inputs.length;
        int width = 0;
        for (var row : inputs) {
            if (width < row.length) {
                width = row.length;
            }
        }
        var ingredients = NonNullList.withSize(height * width, Ingredient.EMPTY);

        for (int a = 0; a < height; a++) {
            for (int b = 0; b < inputs[a].length; b++) {
                var iing = inputs[a][b];
                var ing = iing.asVanillaIngredient();
                int i = a * width + b;
                ingredients.set(i, ing);

            }
        }

        var recipe = new ShapedExtremeCraftingRecipe(id, width, height, ingredients, output.getInternal());
        recipe.setTransformers((x, y, stack) -> inputs[y][x].getRemainingItem(new MCItemStack(stack)).getInternal());

        CraftTweakerAPI.apply(new ActionAddRecipe<>(INSTANCE, recipe));
    }

    @ZenCodeType.Method
    public static void addShapeless(String name, IItemStack output, IIngredient[] inputs) {
        var id = CraftTweakerConstants.rl(INSTANCE.fixRecipeName(name));
        var recipe = new ShapelessExtremeCraftingRecipe(id, toIngredientsList(inputs), output.getInternal());

        recipe.setTransformers((slot, stack) -> inputs[slot].getRemainingItem(new MCItemStack(stack)).getInternal());

        CraftTweakerAPI.apply(new ActionAddRecipe<>(INSTANCE, recipe));
    }

    @ZenCodeType.Method
    public static void addCatalyst(String name, IIngredient[] inputs) {
        var id = CraftTweakerConstants.rl(INSTANCE.fixRecipeName(name));
        var recipe = new InfinityCatalystCraftRecipe(id, toIngredientsList(inputs));

        recipe.setTransformers((slot, stack) -> inputs[slot].getRemainingItem(new MCItemStack(stack)).getInternal());

        CraftTweakerAPI.apply(new ActionAddRecipe<>(INSTANCE, recipe));
    }

    @ZenCodeType.Method
    public static void remove(IItemStack stack) {
        CraftTweakerAPI.apply(new ActionRemoveRecipe<>(INSTANCE, recipe -> recipe.getResultItem(RegistryAccess.EMPTY).is(stack.getInternal().getItem())));
    }

    private static NonNullList<Ingredient> toIngredientsList(IIngredient... ingredients) {
        return Arrays.stream(ingredients)
                .map(IIngredient::asVanillaIngredient)
                .collect(Collectors.toCollection(NonNullList::create));
    }
}
