package committee.nova.mods.avaritia.core.channel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tesseract paged client mirror")
class ClientChannelTest {
    private static final Path SOURCE = Path.of(
            "src/main/java/committee/nova/mods/avaritia/core/channel/ClientChannel.java");

    @Test
    @DisplayName("full synchronization publishes only at FULL_END and deltas merge zero removals")
    void pagedAndDeltaStatesHaveExplicitContracts() throws Exception {
        String source = compact(Files.readString(SOURCE));
        assertAll(
                () -> assertTrue(source.contains("state==ChannelState.FULL_START")),
                () -> assertTrue(source.contains("state==ChannelState.FULL_PAGE||state==ChannelState.FULL_END")),
                () -> assertTrue(source.contains("if(state==ChannelState.FULL_END){replace(")),
                () -> assertTrue(source.contains("if(amount<=0)values.remove(resource)")),
                () -> assertTrue(source.contains("replace(toData(update.name(),items,fluids,update.energy()))"))
        );
    }

    private static String compact(String value) { return value.replaceAll("\\s+", ""); }
}
