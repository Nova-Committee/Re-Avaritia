package committee.nova.mods.avaritia.common.component;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CrystalSpearTargetTest {
    @Test
    void persistentCodecPreservesTargetAndLastKnownPosition() {
        UUID targetId = UUID.randomUUID();
        CrystalSpearTarget expected = target(targetId, 20, -9);

        var encoded = CrystalSpearTarget.CODEC.encodeStart(NbtOps.INSTANCE, expected).getOrThrow();
        CrystalSpearTarget decoded = CrystalSpearTarget.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();

        assertEquals(targetId, decoded.targetId());
        assertEquals(expected.dimension(), decoded.dimension());
        assertEquals(expected.chunkX(), decoded.chunkX());
        assertEquals(expected.chunkZ(), decoded.chunkZ());
        assertEquals(expected.remainingThrusts(), decoded.remainingThrusts());
    }

    @Test
    void networkCodecPreservesTargetAndLastKnownPosition() {
        UUID targetId = UUID.randomUUID();
        CrystalSpearTarget expected = target(targetId, -17, 31);
        var buffer = Unpooled.buffer();
        try {
            CrystalSpearTarget.STREAM_CODEC.encode(buffer, expected);
            CrystalSpearTarget decoded = CrystalSpearTarget.STREAM_CODEC.decode(buffer);

            assertEquals(targetId, decoded.targetId());
            assertEquals(expected.dimension(), decoded.dimension());
            assertEquals(expected.chunkX(), decoded.chunkX());
            assertEquals(expected.chunkZ(), decoded.chunkZ());
            assertEquals(expected.remainingThrusts(), decoded.remainingThrusts());
        } finally {
            buffer.release();
        }
    }

    @Test
    void blockCoordinatesUseMinecraftFloorChunkCoordinates() {
        assertEquals(20, CrystalSpearTarget.blockToChunk(321));
        assertEquals(-9, CrystalSpearTarget.blockToChunk(-129));
        assertEquals(-17, CrystalSpearTarget.blockToChunk(-257));
        assertEquals(31, CrystalSpearTarget.blockToChunk(511));
    }

    @Test
    void successfulThrustsConsumeExactlyFourteenUses() {
        CrystalSpearTarget current = target(UUID.randomUUID(), 0, 0);

        assertEquals(CrystalSpearTarget.MAX_THRUSTS, current.remainingThrusts());
        for (int remaining = CrystalSpearTarget.MAX_THRUSTS - 1; remaining >= 1; remaining--) {
            CrystalSpearTarget next = current.consumeThrust();
            assertNotNull(next);
            current = next;
            assertEquals(remaining, current.remainingThrusts());
        }
        assertNull(current.consumeThrust());
    }

    @Test
    void remainingThrustCountRejectsOutOfRangeState() {
        UUID targetId = UUID.randomUUID();
        Identifier dimension = Identifier.withDefaultNamespace("overworld");

        assertThrows(IllegalArgumentException.class,
                () -> new CrystalSpearTarget(targetId, dimension, 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new CrystalSpearTarget(
                        targetId, dimension, 0, 0, CrystalSpearTarget.MAX_THRUSTS + 1));
    }

    private static CrystalSpearTarget target(UUID targetId, int chunkX, int chunkZ) {
        return new CrystalSpearTarget(
                targetId,
                Identifier.withDefaultNamespace("overworld"),
                chunkX,
                chunkZ,
                CrystalSpearTarget.MAX_THRUSTS);
    }
}
