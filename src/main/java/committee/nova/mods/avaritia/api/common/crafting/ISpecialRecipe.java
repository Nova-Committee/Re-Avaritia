package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public interface ISpecialRecipe extends Recipe<Container> {
    @Override
    default @NotNull ItemStack assemble(@NotNull Container inv, @NotNull RegistryAccess p_267052_) {
        return this.assemble(inv);
    }

    @Override
    default boolean matches(@NotNull Container inv, @NotNull Level level) {
        return this.matches(inv);
    }

    @Override
    default @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull Container inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem().hasCraftingRemainingItem()) {
                remaining.set(i, stack.getRecipeRemainder());
            }
        }

        return remaining;
    }

    ItemStack assemble(Container var1);

    default boolean matches(Container inventory) {
        return this.matches(inventory, 0, inventory.getContainerSize());
    }

    default boolean matches(Container inventory, int startIndex, int endIndex) {
        NonNullList<ItemStack> inputs = NonNullList.create();

        for (int i = startIndex; i < endIndex; ++i) {
            inputs.add(inventory.getItem(i));
        }

        return RecipeMatcher.findMatches(inputs, this.getIngredients()) != null;
    }
}
