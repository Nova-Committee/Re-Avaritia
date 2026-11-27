package committee.nova.mods.avaritia.common.component;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpearTargetReferenceTest {
    @Test
    void persistentCodecPreservesOwnerExpiryAndTargetReference() {
        SpearTargetReference expected = target(UUID.randomUUID(), UUID.randomUUID(), 24, -13, 1_200L);

        var encoded = SpearTargetReference.CODEC.encodeStart(NbtOps.INSTANCE, expected).getOrThrow();
        SpearTargetReference decoded = SpearTargetReference.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();

        assertEquals(expected, decoded);
    }

    @Test
    void networkCodecPreservesOwnerExpiryAndTargetReference() {
        SpearTargetReference expected = target(UUID.randomUUID(), UUID.randomUUID(), -21, 47, 2_400L);
        var buffer = Unpooled.buffer();
        try {
            SpearTargetReference.STREAM_CODEC.encode(buffer, expected);
            assertEquals(expected, SpearTargetReference.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void ownerAndExpiryMustBothMatch() {
        UUID owner = UUID.randomUUID();
        SpearTargetReference reference = target(UUID.randomUUID(), owner, 0, 0, 600L);

        assertTrue(reference.isOwnedBy(owner, 599L));
        assertFalse(reference.isOwnedBy(owner, 600L));
        assertFalse(reference.isOwnedBy(UUID.randomUUID(), 599L));
    }

    @Test
    void blockCoordinatesUseSignedMinecraftChunkCoordinates() {
        assertEquals(20, SpearTargetReference.blockToChunk(321));
        assertEquals(-9, SpearTargetReference.blockToChunk(-129));
        assertEquals(-17, SpearTargetReference.blockToChunk(-257));
        assertEquals(31, SpearTargetReference.blockToChunk(511));
    }

    private static SpearTargetReference target(UUID targetId, UUID ownerId,
                                               int chunkX, int chunkZ, long expiresAt) {
        return new SpearTargetReference(
                targetId,
                ownerId,
                Identifier.withDefaultNamespace("overworld"),
                chunkX,
                chunkZ,
                expiresAt);
    }
}
