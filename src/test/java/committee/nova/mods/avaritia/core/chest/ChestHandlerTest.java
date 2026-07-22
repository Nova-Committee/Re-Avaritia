package committee.nova.mods.avaritia.core.chest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("无尽箱 transfer 存储")
class ChestHandlerTest {
    private static final Path SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/core/chest/ChestHandler.java");
    private static final Path LEGACY_SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/core/chest/ServerChestHandler.java");

    @Test
    @DisplayName("long 数量在边界处饱和且不会经过 int 循环")
    void longAmountMathSaturates() {
        assertAll(
                () -> assertEquals(Long.MAX_VALUE, ChestAmounts.insertable(0L, Long.MAX_VALUE)),
                () -> assertEquals(4L, ChestAmounts.insertable(Long.MAX_VALUE - 4L, 10L)),
                () -> assertEquals(0L, ChestAmounts.insertable(Long.MAX_VALUE, 1L)),
                () -> assertEquals(9L, ChestAmounts.extractable(9L, Long.MAX_VALUE)),
                () -> assertEquals(3L, ChestAmounts.extractable(9L, 3L))
        );
    }

    @Test
    @DisplayName("存储按完整 ItemResource 身份并在每次修改前登记快照")
    void componentIdentityAndSnapshotContract() throws Exception {
        String source = compact(Files.readString(SOURCE));
        assertAll(
                () -> assertTrue(source.contains("LinkedHashMap<ItemResource,Long>items")),
                () -> assertTrue(source.contains("ItemResource.CODEC.fieldOf(\"resource\")")),
                () -> assertTrue(source.contains("ItemResource.STREAM_CODEC.decode(buffer)")),
                () -> assertTrue(source.contains("updateSnapshots(transaction);items.put(resource,stored+inserted)")),
                () -> assertTrue(source.contains("updateSnapshots(transaction);longremaining=stored-extracted")),
                () -> assertTrue(source.contains("protectedvoidonRootCommit(SnapshotoriginalState)"))
        );
    }

    @Test
    @DisplayName("legacy import rejects data that cannot fit without truncation")
    void legacyImportDoesNotSilentlyTruncateVariants() throws Exception {
        String source = compact(Files.readString(LEGACY_SOURCE));
        assertAll(
                () -> assertTrue(source.contains("merged.size()>=ChestHandler.MAX_VARIANTS")),
                () -> assertTrue(source.contains("returnOptional.empty()")),
                () -> assertTrue(source.contains("merged.merge(entry.resource(),entry.amount(),ServerChestHandler::saturatedAdd)"))
        );
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
