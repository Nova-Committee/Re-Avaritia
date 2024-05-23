package committee.nova.mods.avaritia.api.common.crafting;

import committee.nova.mods.avaritia.api.common.item.InvWrapper;
import committee.nova.mods.avaritia.api.util.RecipeMatcher;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
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
        return this.assemble(new InvWrapper(inv));
    }

    @Override
    default boolean matches(Container inv, Level level) {
        return this.matches(new InvWrapper(inv));
    }

    @Override
    default @NotNull NonNullList<ItemStack> getRemainingItems(Container inv) {
        return this.getRemainingItems(new InvWrapper(inv));
    }

    ItemStack assemble(ItemStackHandler var1);

    default boolean matches(ItemStackHandler inventory) {
        return this.matches(inventory, 0, inventory.getSlotCount());
    }

    default boolean matches(ItemStackHandler inventory, int startIndex, int endIndex) {
        NonNullList<ItemStack> inputs = NonNullList.create();

        for (int i = startIndex; i < endIndex; ++i) {
            inputs.add(inventory.getStackInSlot(i));
        }

        return RecipeMatcher.findMatches(inputs, this.getIngredients()) != null;
    }

    default NonNullList<ItemStack> getRemainingItems(ItemStackHandler inventory) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inventory.getSlotCount(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.getItem().hasCraftingRemainingItem()) {
                remaining.set(i, stack.getItem().getCraftingRemainingItem().getDefaultInstance());
            }
        }

        return remaining;
    }
}
