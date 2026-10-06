package committee.nova.mods.avaritia.init.compat.equivox;

import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
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

import java.util.List;

@RecipeTypeMapper(requiredMods = "equivox")
public final class AvaritiaExtremeSmithingMapper implements IRecipeTypeMapper {
    @Override
    public String getName() { return "Avaritia Extreme Smithing"; }

    @Override
    public String getTranslationKey() { return ""; }

    @Override
    public String getDescription() { return "Maps Avaritia smithing recipes, including all three consumed addition slots."; }

    @Override
    public boolean canHandle(RecipeType<?> type) {
        return type == ModRecipeTypes.EXTREME_SMITHING_RECIPE.get();
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> collector,
                                RecipeHolder<?> holder, RegistryAccess registries,
                                INSSFakeGroupManager groups) {
        if (!(holder.value() instanceof ExtremeSmithingRecipe recipe)) return false;
        // matches/assemble use the same addition predicate in each of the three slots.
        return AvaritiaRecipeMapping.map(collector, groups, MappingHelper.recipeId(holder),
                List.of(recipe.template, recipe.base, recipe.additions, recipe.additions, recipe.additions),
                recipe.result.create());
    }
}
