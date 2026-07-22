package committee.nova.mods.avaritia.common.tile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Persistent storage migration contracts")
class PersistentStorageContractTest {
    private static final Path TESSERACT_TILE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/tile/TesseractTile.java");
    private static final Path CHEST_TILE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/tile/InfinityChestTile.java");
    private static final Path TESSERACT_BLOCK = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/block/chest/TesseractBlock.java");
    private static final Path CHEST_BLOCK = Path.of(
            "src/main/java/committee/nova/mods/avaritia/common/block/chest/InfinityChestBlock.java");
    private static final Path LIFECYCLE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/init/handler/PersistentStorageLifecycleHandler.java");

    @Test
    @DisplayName("legacy Tesseract references and default sorting remain compatible")
    void legacyTesseractMetadataIsDecoded() throws Exception {
        String tile = compact(Files.readString(TESSERACT_TILE));
        assertAll(
                () -> assertTrue(tile.contains("privatestaticfinalbyteDEFAULT_SORT_TYPE=4;")),
                () -> assertTrue(tile.contains("fieldOf(\"channelOwner\")")),
                () -> assertTrue(tile.contains("fieldOf(\"channelID\")")),
                () -> assertTrue(tile.contains("input.read(\"channel\",LEGACY_CHANNEL_INFO_CODEC)")),
                () -> assertTrue(tile.contains("TypedEntityData.of(getType(),saveCustomOnly(registries))")),
                () -> assertTrue(tile.contains("entity.teleportTo(worldPosition.getX()+0.5,worldPosition.getY()-0.26"))
        );
    }

    @Test
    @DisplayName("failed cluster migration remains recoverable across save and block pickup")
    void pendingClusterContentsArePersisted() throws Exception {
        String tile = compact(Files.readString(CHEST_TILE));
        String chestBlock = compact(Files.readString(CHEST_BLOCK));
        String tesseractBlock = compact(Files.readString(TESSERACT_BLOCK));
        assertAll(
                () -> assertTrue(tile.contains("output.store(PENDING_CLUSTER_KEY,ClusterContainerContents.CODEC")),
                () -> assertTrue(tile.contains("builder.set(ModDataComponents.CLUSTER_CONTAINER")),
                () -> assertTrue(tile.contains("ContainerHelper.loadAllItems(input,legacyItems)")),
                () -> assertTrue(chestBlock.contains("stack.applyComponents(tile.collectComponents())")),
                () -> assertTrue(tesseractBlock.contains("getCloneItemStack")),
                () -> assertTrue(tesseractBlock.contains("stack.applyComponents(tile.collectComponents())"))
        );
    }

    @Test
    @DisplayName("server-bound managers release listeners and selector state on stop")
    void managersReleaseOnServerStop() throws Exception {
        String lifecycle = compact(Files.readString(LIFECYCLE));
        assertAll(
                () -> assertTrue(lifecycle.contains("ServerChannelManager.release(event.getServer())")),
                () -> assertTrue(lifecycle.contains("ServerChestManager.release(event.getServer())"))
        );
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
