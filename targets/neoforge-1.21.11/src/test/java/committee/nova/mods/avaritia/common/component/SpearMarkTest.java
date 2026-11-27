package committee.nova.mods.avaritia.common.component;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpearMarkTest {
    @Test
    void persistentCodecPreservesOwnerAndExpiryAcrossChunkUnloads() {
        SpearMark expected = new SpearMark(UUID.randomUUID(), 1_234L);

        var encoded = SpearMark.CODEC.codec().encodeStart(NbtOps.INSTANCE, expected).getOrThrow();
        SpearMark decoded = SpearMark.CODEC.codec().parse(NbtOps.INSTANCE, encoded).getOrThrow();

        assertEquals(expected, decoded);
    }

    @Test
    void networkCodecPreservesOwnerAndExpiry() {
        SpearMark expected = new SpearMark(UUID.randomUUID(), 1_234L);
        var buffer = Unpooled.buffer();
        try {
            SpearMark.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, SpearMark.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void markExpiresAtThirtySecondsAndIsOwnerSpecific() {
        UUID owner = UUID.randomUUID();
        long appliedAt = 8_000L;
        SpearMark mark = new SpearMark(owner, appliedAt + SpearMark.DURATION_TICKS);

        assertEquals(600, SpearMark.DURATION_TICKS);
        assertTrue(mark.isOwnedBy(owner, appliedAt + 599L));
        assertFalse(mark.isOwnedBy(owner, appliedAt + 600L));
        assertFalse(mark.isOwnedBy(UUID.randomUUID(), appliedAt + 1L));
        assertEquals(1L, mark.remainingTicks(appliedAt + 599L));
        assertEquals(0L, mark.remainingTicks(appliedAt + 600L));
    }
}
