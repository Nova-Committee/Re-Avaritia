package committee.nova.mods.avaritia.common.item.misc;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeutronRingSavedDataTest {
    @Test
    void adoptMovesEntireLibraryWhenCapacityAllows() {
        NeutronRingSavedData data = new NeutronRingSavedData();
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        assertTrue(data.add(from, "keep", new CompoundTag(), NeutronSpacePreview.EMPTY));
        assertTrue(data.add(to, "existing", new CompoundTag(), NeutronSpacePreview.EMPTY));

        assertTrue(data.adopt(from, to));
        assertEquals(2, data.list(to).size());
        assertTrue(data.list(from).isEmpty());
    }

    @Test
    void adoptLeavesBothLibrariesUnchangedWhenCapacityIsInsufficient() {
        NeutronRingSavedData data = new NeutronRingSavedData();
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        for (int i = 0; i < NeutronRingSavedData.MAX_SPACES; i++) {
            assertTrue(data.add(to, "owned-" + i, new CompoundTag(), NeutronSpacePreview.EMPTY));
        }
        assertTrue(data.add(from, "overflow", new CompoundTag(), NeutronSpacePreview.EMPTY));

        assertFalse(data.adopt(from, to));
        assertEquals(NeutronRingSavedData.MAX_SPACES, data.list(to).size());
        assertEquals(1, data.list(from).size());
        assertEquals("overflow", data.list(from).getFirst().name());
    }
}
