package committee.nova.mods.avaritia.init.compat.projecte;

import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import committee.nova.mods.avaritia.util.IngredientUtils;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.INSSFakeGroupManager;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/7/16 上午12:41
 * @Description:
 */
@RecipeTypeMapper
public class AvaritiaCompressorRecipeMapper implements IRecipeTypeMapper {
    @Override
    public String getName() {
        return "Avaritia Compressor";
    }

    @Override
    public String getDescription() {
        return "Maps avaritia recipes.";
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return recipeType == ModRecipeTypes.COMPRESSOR_RECIPE.get();
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, Recipe<?> recipe, RegistryAccess registryAccess, INSSFakeGroupManager fakeGroupManager) {
        if (!(recipe instanceof CompressorRecipe compressorRecipe)) {
            return false;
        } else {
            boolean handled = false;
            var ingredients = compressorRecipe.getIngredients();
            int timeRequire = compressorRecipe.getTimeRequire();

            for (Ingredient representation : ingredients) {
                for (ItemStack input : representation.getItems()) {
                    NormalizedSimpleStack inputStack = NSSItem.createItem(input);
                    ItemStack output = compressorRecipe.getResultItem();
                    if (!output.isEmpty()) {
                        IngredientUtils ingredientHelper = new IngredientUtils(mapper);
                        ingredientHelper.put(inputStack, compressorRecipe.getInputCount() * timeRequire / 240);
                        if (ingredientHelper.addAsConversion(output)) {
                            handled = true;
                        }
                    }
                }
            }
            return handled;
        }
    }
}
