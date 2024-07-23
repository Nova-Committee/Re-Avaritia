package committee.nova.mods.avaritia.init.compat.projecte;

import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.emc.mappers.recipe.BaseRecipeTypeMapper;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Project: Avaritia
 * Author: cnlimiter
 * CreateTime: 2024/7/16 上午12:41
 * Description:
 */
@RecipeTypeMapper
public final class AvaritiaRecipeMapper extends BaseRecipeTypeMapper {
    @Override
    public String getName() {
        return "Avaritia";
    }

    @Override
    public String getDescription() {
        return "Maps avaritia recipes.";
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return recipeType == ModRecipeTypes.EXTREME_CRAFT_RECIPE.get() || recipeType == ModRecipeTypes.COMPRESSOR_RECIPE.get();
    }
}
