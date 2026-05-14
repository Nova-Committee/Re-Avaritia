package committee.nova.mods.avaritia.init.compat.projecte;

import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.emc.mappers.recipe.BaseRecipeTypeMapper;
import net.minecraft.world.item.crafting.RecipeType;

@RecipeTypeMapper
public class AvaritiaExtremeSmithingMapper extends BaseRecipeTypeMapper {
    @Override
    public String getName() {
        return "Avaritia Extreme Smithing";
    }

    @Override
    public String getTranslationKey() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Maps Avaritia Extreme Smithing recipes to EMC values.";
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return recipeType == ModRecipeTypes.EXTREME_SMITHING_RECIPE.get();
    }

}