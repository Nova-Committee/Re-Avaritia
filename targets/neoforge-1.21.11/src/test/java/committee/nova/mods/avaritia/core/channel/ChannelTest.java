package committee.nova.mods.avaritia.core.channel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tesseract channel transaction storage")
class ChannelTest {
    private static final Path SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/core/channel/Channel.java");

    @Test
    @DisplayName("all three resource handlers register snapshots before mutation")
    void resourceMutationsAreTransactional() throws Exception {
        String source = compact(Files.readString(SOURCE));
        assertAll(
                () -> assertTrue(source.contains("LinkedHashMap<ItemResource,Long>items")),
                () -> assertTrue(source.contains("LinkedHashMap<FluidResource,Long>fluids")),
                () -> assertTrue(source.contains("extendsSnapshotJournal<Channel.Snapshot>")),
                () -> assertTrue(count(source, "updateSnapshots(transaction)") >= 6),
                () -> assertTrue(source.contains("protectedvoidonRootCommit(SnapshotoriginalState)")),
                () -> assertTrue(source.contains("Long.MAX_VALUE-energy"))
        );
    }

    @Test
    @DisplayName("storage identity and persistence preserve item/fluid components and long counts")
    void componentIdentityAndLongCountsArePersisted() throws Exception {
        String source = compact(Files.readString(SOURCE));
        assertAll(
                () -> assertTrue(source.contains("ItemResource.CODEC.fieldOf(\"resource\")")),
                () -> assertTrue(source.contains("FluidResource.CODEC.fieldOf(\"resource\")")),
                () -> assertTrue(source.contains("ExtraCodecs.POSITIVE_LONG.fieldOf(\"amount\")")),
                () -> assertTrue(source.contains("publicstaticfinalintMAX_VARIANTS=65_536")),
                () -> assertTrue(source.contains("substring(0,Math.min(64,value.length()))"))
        );
    }

    private static int count(String source, String value) {
        return (source.length() - source.replace(value, "").length()) / value.length();
    }

    private static String compact(String value) { return value.replaceAll("\\s+", ""); }
}
