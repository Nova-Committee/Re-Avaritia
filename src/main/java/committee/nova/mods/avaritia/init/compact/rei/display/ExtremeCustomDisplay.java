package committee.nova.mods.avaritia.init.compact.rei.display;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.registry.RecipeManagerContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;

public class ExtremeCustomDisplay extends ExtremeTableDisplay {
    private int width;
    private int height;

    public ExtremeCustomDisplay(@Nullable Recipe<?> possibleRecipe, List<EntryIngredient> input, List<EntryIngredient> output) {
        this(null, possibleRecipe, input, output);
    }

    public ExtremeCustomDisplay(@Nullable ResourceLocation location, @Nullable Recipe<?> possibleRecipe, List<EntryIngredient> input, List<EntryIngredient> output) {
        super(input, output, Optional.ofNullable(possibleRecipe));
        BitSet row = new BitSet(9);
        BitSet column = new BitSet(9);
        for (int i = 0; i < 81; i++)
            if (i < input.size()) {
                EntryIngredient stacks = input.get(i);
                if (stacks.stream().anyMatch(stack -> !stack.isEmpty())) {
                    row.set((i - (i % 9)) / 9);
                    column.set(i % 9);
                }
            }
        this.width = column.cardinality();
        this.height = row.cardinality();
    }

    public static ExtremeCustomDisplay simple(List<EntryIngredient> input, List<EntryIngredient> output, Optional<ResourceLocation> location) {
        return new ExtremeCustomDisplay(location.orElse(null),  RecipeManagerContext.getInstance().getRecipeManager().byName.get(location.orElse(null)), input, output);
    }

    @Override
    public int getWidth() {
        return 9;
    }

    @Override
    public int getHeight() {
        return 9;
    }

}
