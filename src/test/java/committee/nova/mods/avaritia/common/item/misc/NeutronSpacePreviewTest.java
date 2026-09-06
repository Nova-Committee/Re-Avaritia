package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeutronSpacePreviewTest {
    @Test
    void saveRoundTripKeepsColumnHeights() {
        NeutronSpacePreview preview = new NeutronSpacePreview(2, 8, 2, 3,
                new int[]{0xFF00AA00, 0, 0xFF2244AA, 0xFF333333},
                new int[]{8, 0, 1, 3});
        NeutronSpacePreview loaded = NeutronSpacePreview.load(preview.save());
        assertEquals(8, loaded.columnHeight(0));
        assertEquals(0, loaded.columnHeight(1));
        assertEquals(1, loaded.columnHeight(2));
        assertEquals(3, loaded.columnHeight(3));
        assertEquals(0xFF00AA00, loaded.top()[0]);
    }

    @Test
    void legacyNbtWithoutHeightBecomesUnitColumns() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("X", 1);
        tag.putInt("Y", 4);
        tag.putInt("Z", 1);
        tag.putInt("Blocks", 1);
        tag.putIntArray("Top", new int[]{0xFFFFFFFF});
        NeutronSpacePreview loaded = NeutronSpacePreview.load(tag);
        assertEquals(1, loaded.columnHeight(0));
    }

    @Test
    void deselectRestoresDefaultCaptureSize() {
        NeutronRingContents selected = NeutronRingContents.owned(UUID.randomUUID())
                .select("space", new NeutronRingContents.Size(1, 2, 3));
        assertEquals(1, selected.size().x());
        NeutronRingContents cleared = selected.deselect();
        assertEquals(NeutronRingContents.Size.DEFAULT, cleared.size());
        assertTrue(cleared.selectedId().isEmpty());
    }
}
