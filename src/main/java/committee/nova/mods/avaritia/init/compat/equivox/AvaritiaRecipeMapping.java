package committee.nova.mods.avaritia.init.compat.equivox;

import committee.nova.mods.avaritia.common.ingredient.StackIngredient;
import com.yaskulsky.equivox.api.mapper.collector.IMappingCollector;
import com.yaskulsky.equivox.api.mapper.recipe.INSSFakeGroupManager;
import com.yaskulsky.equivox.api.nss.NormalizedSimpleStack;
import com.yaskulsky.equivox.api.nss.NSSItem;
import com.yaskulsky.equivox.emc.MappingHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import java.util.Arrays;
import java.util.List;

/** Equivox's vanilla output helper does not recognize Avaritia's recipe classes. */
final class AvaritiaRecipeMapping {
    private AvaritiaRecipeMapping() {
    }

    static boolean map(IMappingCollector<NormalizedSimpleStack, Long> collector,
                       INSSFakeGroupManager groups, Identifier recipeId,
                       List<Ingredient> ingredients, ItemStack result) {
        if (result.isEmpty()) return false;
        Object2IntMap<NormalizedSimpleStack> inputs = new Object2IntOpenHashMap<>();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) continue;
            ItemStack[] alternatives = matchingStacks(ingredient, recipeId);
            if (alternatives.length == 0) return false;
            if (alternatives.length == 1) {
                addInput(inputs, alternatives[0]);
            } else {
                Object2IntMap<NormalizedSimpleStack> choices = new Object2IntOpenHashMap<>();
                for (ItemStack alternative : alternatives) {
                    if (!alternative.isEmpty()) choices.put(NSSItem.createItem(alternative), 1);
                }
                if (choices.isEmpty()) return false;
                var group = groups.getOrCreateFakeGroupDirect(choices, true, true);
                if (group.created()) {
                    for (ItemStack alternative : alternatives) {
                        if (alternative.isEmpty()) continue;
                        Object2IntMap<NormalizedSimpleStack> conversion = new Object2IntOpenHashMap<>();
                        addInput(conversion, alternative);
                        collector.addConversion(1, group.dummy(), conversion);
                    }
                }
                inputs.mergeInt(group.dummy(), 1, Math::addExact);
            }
        }
        collector.addConversion(result.getCount(), NSSItem.createItem(result), inputs);
        return true;
    }

    static ItemStack[] matchingStacks(Ingredient ingredient, Identifier recipeId) {
        var custom = ingredient.getCustomIngredient();
        if (custom instanceof StackIngredient stack) {
            return new ItemStack[]{stack.item()};
        }
        if (custom instanceof DataComponentIngredient components) {
            return components.itemSet().stream()
                    .map(item -> new ItemStack(item, 1, components.components()).create())
                    .toArray(ItemStack[]::new);
        }
        if (custom instanceof CompoundIngredient compound) {
            return compound.children().stream()
                    .flatMap(child -> Arrays.stream(matchingStacks(child, recipeId)))
                    .toArray(ItemStack[]::new);
        }
        return MappingHelper.getMatchingStacks(ingredient, recipeId);
    }

    private static void addInput(Object2IntMap<NormalizedSimpleStack> inputs, ItemStack stack) {
        inputs.mergeInt(NSSItem.createItem(stack), 1, Math::addExact);
        var remainder = stack.getCraftingRemainder();
        if (remainder != null) {
            inputs.mergeInt(NSSItem.createItem(remainder.create()), -1, Math::addExact);
        }
    }
}
