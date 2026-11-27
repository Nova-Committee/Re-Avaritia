package committee.nova.mods.avaritia.common.container.slot;

import committee.nova.mods.avaritia.api.common.crafting.TierInput;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModCraftResultSlotTest {
    @Test
    void countOneNoMatchDoesNotRestoreInput() {
        ItemStack input = new ItemStack(Items.STONE);

        NonNullList<ItemStack> remaining = ModCraftResultSlot.defaultRemainingItems(inputOf(input));

        assertTrue(remaining.get(0).isEmpty());
        assertNotSame(input, remaining.get(0));
    }

    @Test
    void countGreaterThanOneNoMatchDoesNotDuplicateInput() {
        assertNoFallbackInput(2);
        assertNoFallbackInput(64);
    }

    @Test
    void validCraftingRemainderIsPreserved() {
        ItemStack input = new ItemStack(Items.LAVA_BUCKET);

        NonNullList<ItemStack> remaining = ModCraftResultSlot.defaultRemainingItems(inputOf(input));

        assertEquals(1, remaining.size());
        assertTrue(remaining.get(0).is(Items.BUCKET));
        assertEquals(1, remaining.get(0).getCount());
        assertNotSame(input, remaining.get(0));
    }

    @Test
    void removeRevalidatesPickupBeforeTakingStaleResult() {
        SimpleContainer result = new SimpleContainer(new ItemStack(Items.NETHER_STAR));
        ModCraftResultSlot slot = new ModCraftResultSlot(null, new TestMenu(), null, result, 0, 0, 0) {
            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        };

        assertTrue(slot.remove(1).isEmpty());
        assertTrue(result.getItem(0).is(Items.NETHER_STAR));
        assertEquals(1, result.getItem(0).getCount());
    }

    private static void assertNoFallbackInput(int count) {
        ItemStack input = new ItemStack(Items.STONE, count);

        NonNullList<ItemStack> remaining = ModCraftResultSlot.defaultRemainingItems(inputOf(input));

        assertEquals(1, remaining.size());
        assertTrue(remaining.get(0).isEmpty());
        assertNotSame(input, remaining.get(0));
    }

    private static TierInput inputOf(ItemStack stack) {
        return TierInput.of(1, 1, List.of(stack), 1);
    }

    private static final class TestMenu extends AbstractContainerMenu {
        private TestMenu() {
            super(null, 0);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
