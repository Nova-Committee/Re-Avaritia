package committee.nova.mods.avaritia.common.component;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpearTargetReferenceTest {
    @Test
    void persistentCodecPreservesUnlimitedTargetReference() {
        SpearTargetReference expected = target(UUID.randomUUID(), 24, -13);

        var encoded = SpearTargetReference.CODEC.encodeStart(NbtOps.INSTANCE, expected).getOrThrow();
        SpearTargetReference decoded = SpearTargetReference.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();

        assertEquals(expected, decoded);
    }

    @Test
    void networkCodecPreservesUnlimitedTargetReference() {
        SpearTargetReference expected = target(UUID.randomUUID(), -21, 47);
        var buffer = Unpooled.buffer();
        try {
            SpearTargetReference.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, SpearTargetReference.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void blockCoordinatesUseSignedMinecraftChunkCoordinates() {
        assertEquals(20, SpearTargetReference.blockToChunk(321));
        assertEquals(-9, SpearTargetReference.blockToChunk(-129));
        assertEquals(-17, SpearTargetReference.blockToChunk(-257));
        assertEquals(31, SpearTargetReference.blockToChunk(511));
    }

    private static SpearTargetReference target(UUID targetId, int chunkX, int chunkZ) {
        return new SpearTargetReference(
                targetId,
                Identifier.withDefaultNamespace("overworld"),
                chunkX,
                chunkZ);
    }
}
