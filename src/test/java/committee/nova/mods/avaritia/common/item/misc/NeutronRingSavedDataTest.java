package committee.nova.mods.avaritia.common.item.misc;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkHooks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class NeutronRingSavedDataTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        try (MockedStatic<NetworkHooks> ignored = mockStatic(NetworkHooks.class)) {
            Bootstrap.bootStrap();
        }
    }
    @Test
    void adoptMovesEntireLibraryWhenCapacityAllows() {
        NeutronRingSavedData data = new NeutronRingSavedData();
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        assertTrue(data.add(from, "keep", new CompoundTag()));
        assertTrue(data.add(to, "existing", new CompoundTag()));

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
            assertTrue(data.add(to, "owned-" + i, new CompoundTag()));
        }
        assertTrue(data.add(from, "overflow", new CompoundTag()));

        assertFalse(data.adopt(from, to));
        assertEquals(NeutronRingSavedData.MAX_SPACES, data.list(to).size());
        assertEquals(1, data.list(from).size());
        assertEquals("overflow", data.list(from).get(0).name());
    }

    @Test
    void loadIgnoresLegacyRgbPreviewAndKeepsTemplate() {
        UUID storageId = UUID.randomUUID();
        CompoundTag template = new CompoundTag();
        ListTag size = new ListTag();
        size.add(IntTag.valueOf(1));
        size.add(IntTag.valueOf(1));
        size.add(IntTag.valueOf(1));
        template.put("size", size);
        ListTag palette = new ListTag();
        palette.add(NbtUtils.writeBlockState(Blocks.CHEST.defaultBlockState()));
        template.put("palette", palette);
        ListTag blocks = new ListTag();
        CompoundTag cell = new CompoundTag();
        ListTag pos = new ListTag();
        pos.add(IntTag.valueOf(0));
        pos.add(IntTag.valueOf(0));
        pos.add(IntTag.valueOf(0));
        cell.put("pos", pos);
        cell.putInt("state", 0);
        blocks.add(cell);
        template.put("blocks", blocks);

        CompoundTag preview = new CompoundTag();
        preview.putInt("X", 1);
        preview.putInt("Y", 1);
        preview.putInt("Z", 1);
        preview.putInt("Blocks", 1);
        preview.putIntArray("Top", new int[]{0xFF00AA00});
        preview.putIntArray("Height", new int[]{1});

        CompoundTag entry = new CompoundTag();
        entry.putString("Id", "space-1");
        entry.putString("Name", "Chest");
        entry.put("Template", template);
        entry.put("Preview", preview);
        ListTag spaces = new ListTag();
        spaces.add(entry);
        CompoundTag library = new CompoundTag();
        library.put("Id", NbtUtils.createUUID(storageId));
        library.put("Spaces", spaces);
        ListTag libraries = new ListTag();
        libraries.add(library);
        CompoundTag root = new CompoundTag();
        root.put("Libraries", libraries);

        NeutronRingSavedData data = NeutronRingSavedData.load(root);
        assertEquals(1, data.list(storageId).size());
        assertEquals("Chest", data.list(storageId).get(0).name());
        assertEquals("space-1", data.list(storageId).get(0).id());
        assertEquals(Blocks.CHEST.defaultBlockState(),
                NeutronSpacePreview.fromTemplate(data.get(storageId, "space-1").orElseThrow().template(),
                        RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)).stateAt(0, 0, 0));
    }
}
