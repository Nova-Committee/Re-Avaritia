package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import committee.nova.mods.avaritia.api.common.wrapper.ItemHandler;
import committee.nova.mods.avaritia.api.common.wrapper.ContainerItemHandler;
import org.jetbrains.annotations.NotNull;

public interface ISpecialRecipe extends Recipe<Container> {
    @Override
    default @NotNull ItemStack assemble(@NotNull Container inv, @NotNull RegistryAccess p_267052_) {
        return this.assemble(new ContainerItemHandler(inv));
    }

    @Override
    default boolean matches(@NotNull Container inv, @NotNull Level level) {
        return this.matches(new ContainerItemHandler(inv));
    }

    @Override
    default @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull Container inv) {
        return this.getRemainingItems(new ContainerItemHandler(inv));
    }

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

    private static boolean matchesIngredients(java.util.List<ItemStack> inputs, java.util.List<net.minecraft.world.item.crafting.Ingredient> ingredients, int index, boolean[] used) {
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
            if (stack.getItem().hasCraftingRemainingItem()) {
                remaining.set(i, new ItemStack(stack.getItem().getCraftingRemainingItem()));
            }
        }

        return remaining;
    }
}
