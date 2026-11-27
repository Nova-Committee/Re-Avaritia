package committee.nova.mods.avaritia.init.compat.equivox;

import committee.nova.mods.avaritia.api.common.crafting.ITierCraftingRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import com.yaskulsky.equivox.api.mapper.collector.IMappingCollector;
import com.yaskulsky.equivox.api.mapper.recipe.INSSFakeGroupManager;
import com.yaskulsky.equivox.api.mapper.recipe.IRecipeTypeMapper;
import com.yaskulsky.equivox.api.mapper.recipe.RecipeTypeMapper;
import com.yaskulsky.equivox.api.nss.NormalizedSimpleStack;
import com.yaskulsky.equivox.emc.MappingHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

@RecipeTypeMapper(requiredMods = "equivox")
public final class AvaritiaExtremeRecipeMapper implements IRecipeTypeMapper {
    @Override
    public String getName() { return "Avaritia Extreme"; }

    @Override
    public String getTranslationKey() { return ""; }

    @Override
    public String getDescription() { return "Maps Avaritia tiered shaped and shapeless recipes."; }

    @Override
    public boolean canHandle(RecipeType<?> type) {
        return type == ModRecipeTypes.CRAFTING_TABLE_RECIPE.get();
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> collector,
                                RecipeHolder<?> holder, RegistryAccess registries,
                                INSSFakeGroupManager groups) {
        if (!(holder.value() instanceof ITierCraftingRecipe recipe) || recipe.isSpecial()) return false;
        return AvaritiaRecipeMapping.map(collector, groups, MappingHelper.recipeId(holder),
                recipe.getIngredients(), recipe.getResultItem(registries));
    }
}
