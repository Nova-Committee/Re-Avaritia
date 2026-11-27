package committee.nova.mods.avaritia.common.component;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InfinityChestReferenceTest {

    @Test
    void codecRoundTripsStableReferenceFields() {
        InfinityChestReference reference = new InfinityChestReference(
                UUID.randomUUID(), true, "diamond", (byte) 2, UUID.randomUUID());

        var encoded = InfinityChestReference.CODEC.encodeStart(JsonOps.INSTANCE, reference).getOrThrow();
        var decoded = InfinityChestReference.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(reference, decoded);
    }

    @Test
    void normalizesUntrustedItemMetadata() {
        UUID owner = UUID.randomUUID();
        UUID channel = UUID.randomUUID();

        InfinityChestReference nullFilter = new InfinityChestReference(owner, false, null, (byte) -1, channel);
        assertEquals("", nullFilter.filter());
        assertEquals(4, nullFilter.sortType());

        InfinityChestReference longFilter = new InfinityChestReference(owner, false, "x".repeat(100), (byte) 7, channel);
        assertEquals(64, longFilter.filter().length());
        assertEquals(7, longFilter.sortType());
    }

    @Test
    void codecRejectsOverlongFilterInsteadOfSilentlyAcceptingSaveData() {
        InfinityChestReference reference = new InfinityChestReference(
                UUID.randomUUID(), false, "valid", (byte) 4, UUID.randomUUID());
        JsonObject encoded = InfinityChestReference.CODEC.encodeStart(JsonOps.INSTANCE, reference)
                .getOrThrow().getAsJsonObject();
        encoded.addProperty("filter", "x".repeat(65));

        assertTrue(InfinityChestReference.CODEC.parse(JsonOps.INSTANCE, encoded).error().isPresent());
    }
}
