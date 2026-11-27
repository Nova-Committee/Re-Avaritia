package committee.nova.mods.avaritia.init.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.action.recipe.ActionRemoveRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.common.crafting.ITierCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.openzen.zencode.java.ZenCodeType;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/17 8:39
 * Version: 1.0
 */
@ZenCodeType.Name("mods.avaritia.CraftingTable")
@ZenRegister
public class CraftingTable implements IRecipeManager<ITierCraftingRecipe> {
    @ZenCodeType.Method
    public void addShaped(String name, int tier, IItemStack output, IIngredient[][] inputs) {
        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
        if (tier > 4 || tier < 0) {
            tier = 0;
            CraftTweakerAPI.getLogger(Const.MOD_ID).error("Unable to assign a tier to the Table Recipe for stack " + output.getCommandString() + ". Tier cannot be greater than 4 or less than 0.");
        }
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

        var pattern = new ShapedRecipePattern(width, height, ingredients, Optional.empty());
        var recipe = new ShapedTableCraftingRecipe(pattern, output.getInternal(), tier, false);
        recipe.setTransformers((x, y, stack) -> inputs[y][x].getRemainingItem(new MCItemStack(stack)).getInternal());

        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
    }

    @ZenCodeType.Method
    public void addShapeless(String name, IItemStack output, IIngredient[] inputs) {
        addShapeless(name, 0, output, inputs);
    }

    @ZenCodeType.Method
    public void addShapeless(String name, int tier, IItemStack output, IIngredient[] inputs) {
        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
        if (tier > 4 || tier < 0) {
            tier = 0;
            CraftTweakerAPI.getLogger(Const.MOD_ID).error("Unable to assign a tier to the Table Recipe for stack " + output.getCommandString() + ". Tier cannot be greater than 4 or less than 0.");
        }
        var recipe = new ShapelessTableCraftingRecipe(toIngredientsList(inputs), output.getInternal(), tier);

        recipe.setTransformers((slot, stack) -> inputs[slot].getRemainingItem(new MCItemStack(stack)).getInternal());

        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
    }

    @ZenCodeType.Method
    public void addCatalyst(String name, IIngredient[] inputs, int count) {
        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
        var recipe = new InfinityCatalystCraftRecipe("default", toIngredientsList(inputs), count);

        recipe.setTransformers((slot, stack) -> inputs[slot].getRemainingItem(new MCItemStack(stack)).getInternal());

        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
    }

    @ZenCodeType.Method
    public void addCatalyst(String name, String group, IIngredient[] inputs, int count) {
        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
        var recipe = new InfinityCatalystCraftRecipe(group, toIngredientsList(inputs), count);

        recipe.setTransformers((slot, stack) -> inputs[slot].getRemainingItem(new MCItemStack(stack)).getInternal());

        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
    }

    @ZenCodeType.Method
    public void addEnternal(String name, IIngredient[] inputs, int count) {
        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
        var recipe = new EternalSingularityCraftRecipe(toIngredientsList(inputs), count);

        recipe.setTransformers((slot, stack) -> inputs[slot].getRemainingItem(new MCItemStack(stack)).getInternal());

        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
    }

    @ZenCodeType.Method
    public void addEnternal(String name, IIngredient[] inputs) {
        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
        var recipe = new EternalSingularityCraftRecipe(toIngredientsList(inputs), 1);

        recipe.setTransformers((slot, stack) -> inputs[slot].getRemainingItem(new MCItemStack(stack)).getInternal());

        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
    }


    @ZenCodeType.Method
    public void remove(IItemStack stack) {
        CraftTweakerAPI.apply(new ActionRemoveRecipe<>(this, recipe -> recipe.value().getResultItem(RegistryAccess.EMPTY).is(stack.getInternal().getItem())));
    }

    private static NonNullList<Ingredient> toIngredientsList(IIngredient... ingredients) {
        return Arrays.stream(ingredients)
                .map(IIngredient::asVanillaIngredient)
                .collect(Collectors.toCollection(NonNullList::create));
    }

    @Override
    public RecipeType<ITierCraftingRecipe> getRecipeType() {
        return ModRecipeTypes.CRAFTING_TABLE_RECIPE.get();
    }
}
