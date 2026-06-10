package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.common.item.misc.InfinityClockTimes;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class C2SSetTimePacketTest {
    private static final Path PACKET_SOURCE = Path.of("src/main/java/committee/nova/mods/avaritia/common/net/C2SSetTimePacket.java");
    private static final Path SCREEN_SOURCE = Path.of("src/main/java/committee/nova/mods/avaritia/client/screen/InfinityClockScreen.java");

    @Test
    void resolvesSelectedTimeWithinCurrentDay() {
        long currentTicks = 245123L;

        assertEquals(240000L, InfinityClockTimes.resolveSelectedDayTime(currentTicks, InfinityClockTimes.SUNRISE));
        assertEquals(246000L, InfinityClockTimes.resolveSelectedDayTime(currentTicks, InfinityClockTimes.DAY));
        assertEquals(252000L, InfinityClockTimes.resolveSelectedDayTime(currentTicks, InfinityClockTimes.SUNSET));
        assertEquals(262000L, InfinityClockTimes.resolveSelectedDayTime(currentTicks, InfinityClockTimes.LATE_NIGHT));
    }

    @Test
    void handlerSetsAbsoluteClockTimeWithoutDefaultClockWarnings() throws IOException {
        String source = compact(Files.readString(PACKET_SOURCE));

        assertTrue(source.contains("setTotalTicks(clockHolder,InfinityClockTimes.resolveSelectedDayTime"));
        assertFalse(source.contains("addTicks("));
        assertFalse(source.contains("no_default_clock"));
    }

    @Test
    void screenButtonsUseSharedClockTimeConstants() throws IOException {
        String source = compact(Files.readString(SCREEN_SOURCE));

        assertTrue(source.contains("InfinityClockTimes.SUNRISE"));
        assertTrue(source.contains("InfinityClockTimes.DAY"));
        assertTrue(source.contains("InfinityClockTimes.SUNSET"));
        assertTrue(source.contains("InfinityClockTimes.NIGHT"));
        assertTrue(source.contains("InfinityClockTimes.MIDNIGHT"));
        assertTrue(source.contains("InfinityClockTimes.LATE_NIGHT"));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
