package committee.nova.mods.avaritia.common.crafting.recipe;

import committee.nova.mods.avaritia.api.common.crafting.TierInput;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoConsumeCatalystShapedRecipeTest {
    @Test
    void catalystRemainderPreservesCountAfterConsumption() {
        assertAll(
                () -> assertCatalystCountConserved(1),
                () -> assertCatalystCountConserved(2),
                () -> assertCatalystCountConserved(64)
        );
    }

    @Test
    void nonCatalystSlotsRetainSuperRemainderBehavior() {
        ItemStack catalyst = new ItemStack(Items.NETHER_STAR);
        ItemStack lavaBucket = new ItemStack(Items.LAVA_BUCKET);
        TierInput input = TierInput.of(2, 1, List.of(catalyst, lavaBucket), 1);
        NonNullList<ItemStack> superRemaining = recipe(2).getRemainingItems(input);
        ItemStack expectedBucket = superRemaining.get(1);

        NonNullList<ItemStack> remaining = NoConsumeCatalystShapedRecipe.retainSingleCatalyst(
                input,
                superRemaining,
                Items.NETHER_STAR
        );

        assertSame(expectedBucket, remaining.get(1));
        assertTrue(remaining.get(1).is(Items.BUCKET));
        assertEquals(1, remaining.get(1).getCount());
        assertNotSame(input.getItem(1), remaining.get(1));
    }

    private static void assertCatalystCountConserved(int inputCount) {
        ItemStack catalyst = new ItemStack(Items.NETHER_STAR, inputCount);
        TierInput input = TierInput.of(1, 1, List.of(catalyst), 1);

        NonNullList<ItemStack> remaining = NoConsumeCatalystShapedRecipe.retainSingleCatalyst(
                input,
                recipe(1).getRemainingItems(input),
                Items.NETHER_STAR
        );
        ItemStack remainder = remaining.get(0);

        assertTrue(remainder.is(Items.NETHER_STAR));
        assertEquals(1, remainder.getCount());
        assertNotSame(input.getItem(0), remainder);

        ItemStack slotAfterCraft = input.getItem(0).copy();
        slotAfterCraft.shrink(1);
        if (slotAfterCraft.isEmpty()) {
            slotAfterCraft = remainder.copy();
        } else {
            assertTrue(ItemStack.isSameItemSameComponents(slotAfterCraft, remainder));
            slotAfterCraft.grow(remainder.getCount());
        }

        assertTrue(slotAfterCraft.is(Items.NETHER_STAR));
        assertEquals(inputCount, slotAfterCraft.getCount());
    }

    private static ShapedTableCraftingRecipe recipe(int width) {
        ShapedRecipePattern pattern = new ShapedRecipePattern(
                width,
                1,
                NonNullList.withSize(width, Ingredient.EMPTY),
                Optional.empty()
        );
        return new ShapedTableCraftingRecipe(pattern, ItemStack.EMPTY, 1, false);
    }
}
