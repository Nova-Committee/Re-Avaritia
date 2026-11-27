package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import committee.nova.mods.avaritia.api.common.wrapper.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ISpecialRecipe extends Recipe<CraftingInput> {

    static ItemStackHandler toHandler(CraftingInput input) {
        NonNullList<ItemStack> items = NonNullList.create();
        items.addAll(input.items());
        return new ItemStackHandler(items);
    }

    @Override
    default @NotNull ItemStack assemble(@NotNull CraftingInput input) {
        return this.assemble(toHandler(input));
    }

    @Override
    default boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return this.matches(toHandler(input));
    }

    default @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        return this.getRemainingItems(toHandler(input));
    }

    @NotNull NonNullList<Ingredient> getIngredients();

    ItemStack assemble(ItemHandler var1);

    default boolean matches(ItemHandler inventory) {
        return this.matches(inventory, 0, inventory.getSlots());
    }

    default boolean matches(ItemHandler inventory, int startIndex, int endIndex) {
        NonNullList<ItemStack> inputs = NonNullList.create();

        for (int i = startIndex; i < endIndex; ++i) {
            inputs.add(inventory.getStackInSlot(i));
        }

        return matchesIngredients(inputs, this.getIngredients(), 0, new boolean[inputs.size()]);
    }

    private static boolean matchesIngredients(List<ItemStack> inputs, List<Ingredient> ingredients, int index, boolean[] used) {
        if (inputs.size() != ingredients.size()) return false;
        if (index == ingredients.size()) return true;
        for (int i = 0; i < inputs.size(); i++) {
            if (!used[i] && ingredients.get(index).test(inputs.get(i))) {
                used[i] = true;
                if (matchesIngredients(inputs, ingredients, index + 1, used)) return true;
                used[i] = false;
            }
        }
        return false;
    }

    default NonNullList<ItemStack> getRemainingItems(ItemHandler inventory) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inventory.getSlots(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            ItemStackTemplate template = stack.getItem().getCraftingRemainder();
            if (template != null) {
                remaining.set(i, template.create());
            }
        }

        return remaining;
    }
}