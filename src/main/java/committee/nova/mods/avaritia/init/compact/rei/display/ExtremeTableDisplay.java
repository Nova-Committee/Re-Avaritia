package committee.nova.mods.avaritia.init.compact.rei.display;

import committee.nova.mods.avaritia.init.compact.rei.ServerREIPlugin;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuSerializationContext;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;

import java.util.*;



public abstract class ExtremeTableDisplay<C extends Recipe<?>> extends BasicDisplay implements SimpleGridMenuDisplay {
    Optional<C> recipe;

    public ExtremeTableDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<C> recipe) {
        super(inputs, outputs);
        this.recipe = recipe;
    }

    public List<InputIngredient<EntryStack<?>>> getInputIngredients(MenuSerializationContext<?, ?, ?> context, MenuInfo<?, ?> info, boolean fill) {
        int craftingWidth = 9, craftingHeight = 9;

        if (info instanceof SimpleGridMenuInfo && fill) {
            craftingWidth = ((SimpleGridMenuInfo<AbstractContainerMenu, ?>) info).getCraftingWidth(context.getMenu());
            craftingHeight = ((SimpleGridMenuInfo<AbstractContainerMenu, ?>) info).getCraftingHeight(context.getMenu());
        }

        return getInputIngredients(craftingWidth, craftingHeight);
    }

    public static int getSlotWithSize(int recipeWidth, int index, int craftingGridWidth) {
        int x = index % recipeWidth;
        int y = (index - x) / recipeWidth;
        return craftingGridWidth * y + x;
    }

    public boolean isShapeless() {
        return false;
    }

    public static int getSlotWithSize(ExtremeTableDisplay<?> display, int index, int craftingGridWidth) {
        return getSlotWithSize(display.getWidth(), index, craftingGridWidth);
    }

    public <T extends AbstractContainerMenu> List<EntryIngredient> getOrganisedInputEntries(int menuWidth, int menuHeight) {
        List<EntryIngredient> list = new ArrayList<>(menuWidth * menuHeight);
        for (int i = 0; i < menuWidth * menuHeight; i++) {
            list.add(EntryIngredient.empty());
        }
        for (int i = 0; i < getInputEntries().size(); i++) {
            list.set(getSlotWithSize(this, i, menuWidth), getInputEntries().get(i));
        }
        return list;
    }

    public static Serializer<ExtremeTableDisplay<?>> serializer () {
        return Serializer.<ExtremeTableDisplay<?>>ofSimple(ExtremeCustomDisplay::simple).
                inputProvider(display -> display.getOrganisedInputEntries(9, 9));
    }


    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ServerREIPlugin.EXTREME_TABLE;
    }

    public List<InputIngredient<EntryStack<?>>> getInputIngredients(int craftingWidth, int craftingHeight) {
        int inputWidth = getInputWidth(craftingWidth, craftingHeight);
        int inputHeight = getInputHeight(craftingWidth, craftingHeight);

        Map<IntIntPair, InputIngredient<EntryStack<?>>> grid = new HashMap<>();

        List<EntryIngredient> inputEntries = getInputEntries();
        for (int i = 0; i < inputEntries.size(); i++) {
            EntryIngredient stacks = inputEntries.get(i);
            if (stacks.isEmpty()) {
                continue;
            }
            int index = getSlotWithSize(inputWidth, i, craftingWidth);
            grid.put(new IntIntImmutablePair(i % inputWidth, i / inputWidth), InputIngredient.of(index, stacks));
        }

        List<InputIngredient<EntryStack<?>>> list = new ArrayList<>(craftingWidth * craftingHeight);
        for (int i = 0, n = craftingWidth * craftingHeight; i < n; i++) {
            list.add(InputIngredient.empty(i));
        }

        for (int x = 0; x < craftingWidth; x++) {
            for (int y = 0; y < craftingHeight; y++) {
                InputIngredient<EntryStack<?>> ingredient = grid.get(new IntIntImmutablePair(x, y));
                if (ingredient != null) {
                    int index = craftingWidth * y + x;
                    list.set(index, ingredient);
                }
            }
        }

        return list;
    }
}
