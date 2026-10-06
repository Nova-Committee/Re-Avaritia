package committee.nova.mods.avaritia.init.compat.equivox;

import committee.nova.mods.avaritia.api.common.crafting.ICompressorRecipe;
import committee.nova.mods.avaritia.init.registry.ModRecipeTypes;
import com.yaskulsky.equivox.api.mapper.collector.IMappingCollector;
import com.yaskulsky.equivox.api.mapper.recipe.INSSFakeGroupManager;
import com.yaskulsky.equivox.api.mapper.recipe.IRecipeTypeMapper;
import com.yaskulsky.equivox.api.mapper.recipe.RecipeTypeMapper;
import com.yaskulsky.equivox.api.nss.NormalizedSimpleStack;
import com.yaskulsky.equivox.api.nss.NSSItem;
import com.yaskulsky.equivox.emc.MappingHelper;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

@RecipeTypeMapper(requiredMods = "equivox")
public final class AvaritiaCompressorRecipeMapper implements IRecipeTypeMapper {
    @Override
    public String getName() { return "Avaritia Compressor"; }

    @Override
    public String getTranslationKey() { return ""; }

    @Override
    public String getDescription() { return "Maps Avaritia compressor recipes with the ProjectE time-cost policy."; }

    @Override
    public boolean canHandle(RecipeType<?> type) {
        return type == ModRecipeTypes.COMPRESSOR_RECIPE.get();
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> collector,
                                RecipeHolder<?> holder, RegistryAccess registries,
                                INSSFakeGroupManager groups) {
        if (!(holder.value() instanceof ICompressorRecipe recipe)) return false;
        var result = recipe.getResultItem(registries);
        if (result.isEmpty()) return false;
        long amount = (long) recipe.getInputCount() * recipe.getTimeCost() / 240;
        if (amount <= 0 || amount > Integer.MAX_VALUE) return false;
        var output = NSSItem.createItem(result);
        var recipeId = MappingHelper.recipeId(holder);
        boolean handled = false;
        for (var ingredient : recipe.getIngredients()) {
            for (var input : AvaritiaRecipeMapping.matchingStacks(ingredient, recipeId)) {
                if (input.isEmpty()) continue;
                var inputs = new Object2IntArrayMap<NormalizedSimpleStack>(1);
                inputs.put(NSSItem.createItem(input), (int) amount);
                collector.addConversion(result.getCount(), output, inputs);
                handled = true;
            }
        }
        return handled;
    }
}
