package committee.nova.mods.avaritia.init.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.action.recipe.ActionRemoveRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/17 8:35
 * Version: 1.0
 */
@ZenCodeType.Name("mods.avaritia.CompressionCrafting")
@ZenRegister
public class CompressionCrafting implements IRecipeManager<ICompressorRecipe> {
    private static final CompressionCrafting INSTANCE = new CompressionCrafting();

    @Override
    public RecipeType<ICompressorRecipe> getRecipeType() {
        return ModRecipeTypes.COMPRESSOR_RECIPE.get();
    }
    @ZenCodeType.Method
    public static void addRecipe(String name, IIngredient input, IItemStack output, int inputCount, int timeCost) {
        var id = CraftTweakerConstants.rl(INSTANCE.fixRecipeName(name));
        var recipe = new CompressorRecipe(id, input.asVanillaIngredient(), output.getInternal(), inputCount, timeCost);

        CraftTweakerAPI.apply(new ActionAddRecipe<>(INSTANCE, recipe));
    }

    @ZenCodeType.Method
    public static void remove(IItemStack stack) {
        CraftTweakerAPI.apply(new ActionRemoveRecipe<>(INSTANCE, recipe -> recipe.getResultItem(RegistryAccess.EMPTY).is(stack.getInternal().getItem())));
    }
}
