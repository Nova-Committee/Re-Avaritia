package committee.nova.mods.avaritia.init.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.action.recipe.ActionRemoveRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Description:
 * @author cnlimiter
 * Date: 2024/12/21 8:35
 * Version: 1.0
 */
@ZenCodeType.Name("mods.avaritia.ExtremeSmithing")
@ZenRegister
public class ExtremeSmithingCrafting implements IRecipeManager<ExtremeSmithingRecipe> {
    private static final ExtremeSmithingCrafting INSTANCE = new ExtremeSmithingCrafting();

    @ZenCodeType.Method
    public static void addRecipe(String name, IIngredient template, IIngredient base, IIngredient additions, IItemStack output) {
        var id = CraftTweakerConstants.rl(INSTANCE.fixRecipeName(name));
        var recipe = new ExtremeSmithingRecipe(id, template.asVanillaIngredient(), base.asVanillaIngredient(), additions.asVanillaIngredient(), output.getInternal());

        CraftTweakerAPI.apply(new ActionAddRecipe<>(INSTANCE, recipe));
    }

    @ZenCodeType.Method
    public static void remove(IItemStack stack) {
        CraftTweakerAPI.apply(new ActionRemoveRecipe<>(INSTANCE, recipe -> recipe.getResultItem(RegistryAccess.EMPTY).is(stack.getInternal().getItem())));
    }

    @Override
    public RecipeType<ExtremeSmithingRecipe> getRecipeType() {
        return ModRecipeTypes.EXTREME_SMITHING_RECIPE.get();
    }
}
