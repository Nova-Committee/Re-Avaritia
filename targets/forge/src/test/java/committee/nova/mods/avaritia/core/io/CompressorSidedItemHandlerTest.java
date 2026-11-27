package committee.nova.mods.avaritia.core.io;

import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkHooks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class CompressorSidedItemHandlerTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        // Raw JUnit does not apply Forge's event transformations.
        try (var ignored = mockStatic(NetworkHooks.class)) {
            Bootstrap.bootStrap();
        }
    }

    @Test
    void inputModesAcceptMaterialsButCannotExtractEitherSlot() {
        for (var mode : new SideConfiguration.SideMode[]{
                SideConfiguration.SideMode.PASSIVE_INPUT, SideConfiguration.SideMode.ACTIVE_INPUT}) {
            var inventory = NeutronCompressorTile.createInventoryHandler();
            inventory.setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
            var handler = new CompressorSidedItemHandler(inventory, () -> mode);

            assertEquals(1, handler.getSlots());
            assertTrue(handler.getStackInSlot(0).isEmpty());
            assertTrue(handler.insertItem(0, new ItemStack(Items.PAPER, 8), false).isEmpty());
            assertEquals(8, inventory.getStackInSlot(1).getCount());
            assertTrue(handler.extractItem(0, 64, false).isEmpty());
            assertEquals(4, inventory.getStackInSlot(0).getCount());
            assertEquals(8, inventory.getStackInSlot(1).getCount());
            assertThrows(IndexOutOfBoundsException.class, () -> handler.extractItem(1, 64, false));
        }
    }

    @Test
    void outputModesRejectMaterialsAndRespectExtractionSimulation() {
        for (var mode : new SideConfiguration.SideMode[]{
                SideConfiguration.SideMode.PASSIVE_OUTPUT, SideConfiguration.SideMode.ACTIVE_OUTPUT}) {
            var inventory = NeutronCompressorTile.createInventoryHandler();
            inventory.setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
            var handler = new CompressorSidedItemHandler(inventory, () -> mode);

            assertEquals(1, handler.getSlots());
            assertFalse(handler.isItemValid(0, new ItemStack(Items.PAPER)));
            assertEquals(8, handler.insertItem(0, new ItemStack(Items.PAPER, 8), false).getCount());
            assertTrue(inventory.getStackInSlot(1).isEmpty());
            assertEquals(4, handler.extractItem(0, 64, true).getCount());
            assertEquals(4, inventory.getStackInSlot(0).getCount());
            assertEquals(4, handler.extractItem(0, 64, false).getCount());
            assertTrue(inventory.getStackInSlot(0).isEmpty());
        }
    }

    @Test
    void mixedModesKeepInputFirstWithoutBypassingOutputSlotRestrictions() {
        for (var mode : new SideConfiguration.SideMode[]{
                SideConfiguration.SideMode.PASSIVE_MIXIN, SideConfiguration.SideMode.ACTIVE_MIXIN}) {
            var inventory = NeutronCompressorTile.createInventoryHandler();
            inventory.setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
            inventory.setStackInSlot(1, new ItemStack(Items.PAPER, 8));
            var handler = new CompressorSidedItemHandler(inventory, () -> mode);

            assertEquals(2, handler.getSlots());
            assertTrue(handler.getStackInSlot(0).is(Items.PAPER));
            assertTrue(handler.getStackInSlot(1).is(Items.BEDROCK));
            assertTrue(handler.extractItem(0, 64, false).isEmpty());
            assertEquals(8, inventory.getStackInSlot(1).getCount());
            assertEquals(8, handler.insertItem(1, new ItemStack(Items.PAPER, 8), false).getCount());
            assertEquals(4, handler.extractItem(1, 64, false).getCount());
            assertTrue(handler.insertItem(0, new ItemStack(Items.PAPER, 8), true).isEmpty());
            assertEquals(8, inventory.getStackInSlot(1).getCount());
            assertTrue(handler.insertItem(0, new ItemStack(Items.PAPER, 8), false).isEmpty());
            assertEquals(16, inventory.getStackInSlot(1).getCount());
        }
    }

    @Test
    void retainedViewUsesCurrentModeInsteadOfKeepingOldPermissions() {
        var inventory = NeutronCompressorTile.createInventoryHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
        inventory.setStackInSlot(1, new ItemStack(Items.PAPER, 8));
        var mode = new AtomicReference<>(SideConfiguration.SideMode.PASSIVE_MIXIN);
        var handler = new CompressorSidedItemHandler(inventory, mode::get);

        mode.set(SideConfiguration.SideMode.PASSIVE_INPUT);
        assertEquals(1, handler.getSlots());
        assertTrue(handler.extractItem(0, 64, false).isEmpty());
        assertEquals(4, inventory.getStackInSlot(0).getCount());
        mode.set(SideConfiguration.SideMode.PASSIVE_OUTPUT);
        assertTrue(handler.getStackInSlot(0).is(Items.BEDROCK));
        assertEquals(8, handler.insertItem(0, new ItemStack(Items.PAPER, 8), false).getCount());
        mode.set(SideConfiguration.SideMode.OFF);
        assertEquals(0, handler.getSlots());
        assertThrows(IndexOutOfBoundsException.class, () -> handler.extractItem(0, 64, false));
        assertEquals(4, inventory.getStackInSlot(0).getCount());
        assertEquals(8, inventory.getStackInSlot(1).getCount());
    }

    @Test
    void delegatesUnderlyingInventoryPermissionsAndLimits() {
        var inventory = NeutronCompressorTile.createInventoryHandler();
        inventory.setCanInsert((slot, stack) -> slot == 1 && stack.is(Items.IRON_INGOT));
        inventory.setCanExtract(slot -> false);
        inventory.addSlotLimit(1, 16);
        inventory.setStackInSlot(0, new ItemStack(Items.BEDROCK, 4));
        var handler = new CompressorSidedItemHandler(inventory, () -> SideConfiguration.SideMode.PASSIVE_MIXIN);

        assertFalse(handler.isItemValid(0, new ItemStack(Items.PAPER)));
        assertEquals(8, handler.insertItem(0, new ItemStack(Items.PAPER, 8), false).getCount());
        assertEquals(16, handler.getSlotLimit(0));
        assertEquals(4, handler.insertItem(0, new ItemStack(Items.IRON_INGOT, 20), false).getCount());
        assertEquals(16, inventory.getStackInSlot(1).getCount());
        assertTrue(handler.extractItem(1, 64, false).isEmpty());
        assertEquals(4, inventory.getStackInSlot(0).getCount());
    }
}
