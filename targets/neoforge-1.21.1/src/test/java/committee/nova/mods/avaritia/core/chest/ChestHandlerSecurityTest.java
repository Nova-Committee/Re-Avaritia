package committee.nova.mods.avaritia.core.chest;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChestHandlerSecurityTest {

    @Test
    void normalizesIdentityStackAndOnlyExtractsRequestedAmount() {
        TestChestHandler handler = new TestChestHandler();
        ItemSuper initial = ItemSuper.of(new ItemStack(Items.STONE, 64));
        assertNotNull(initial);

        assertEquals(64, handler.addItem(initial, 64));
        ItemSuper forgedRequest = new ItemSuper(new ItemStack(Items.STONE, 64), Long.MAX_VALUE);
        ItemSuper stored = handler.resolveStoredItem(forgedRequest);
        assertNotNull(stored);
        assertEquals(1, stored.getStack().getCount());

        ItemStack extracted = handler.takeItem(stored, 1);
        assertEquals(1, extracted.getCount());
        assertEquals(63, handler.storageItems.get(stored));
    }

    @Test
    void rejectsNegativeMutationAmounts() {
        TestChestHandler handler = new TestChestHandler();
        ItemSuper item = new ItemSuper(new ItemStack(Items.STONE), 1);

        assertEquals(0, handler.addItem(item, -1));
        assertTrue(handler.storageItems.isEmpty());

        handler.addItem(item, 8);
        handler.updateItemKeys();
        assertTrue(handler.takeItem(item, -1).isEmpty());
        assertTrue(handler.extractItem(27, -1, false).isEmpty());
        handler.removeItem(item, -1);
        assertEquals(8, handler.storageItems.get(item));
    }

    private static final class TestChestHandler extends ChestHandler {
        @Override
        public boolean isRemoved() {
            return false;
        }
    }
}
