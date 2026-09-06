package committee.nova.mods.avaritia.common.component;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InfinityBucketControlTest {

    @Test
    void codecIgnoresLegacyForceAndPreservesCreatureIndex() {
        assertEquals(new InfinityBucketControl(true, 3), parseLegacy(true, true, 3));
        assertEquals(new InfinityBucketControl(false, 2), parseLegacy(false, false, 2));
    }

    private static InfinityBucketControl parseLegacy(boolean force, boolean creature, int index) {
        JsonObject legacy = new JsonObject();
        legacy.addProperty("force", force);
        legacy.addProperty("creature", creature);
        legacy.addProperty("index", index);
        return InfinityBucketControl.CODEC.parse(JsonOps.INSTANCE, legacy).getOrThrow();
    }
}
