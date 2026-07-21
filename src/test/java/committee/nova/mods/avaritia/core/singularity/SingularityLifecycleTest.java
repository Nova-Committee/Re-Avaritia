package committee.nova.mods.avaritia.core.singularity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SingularityLifecycleTest {
    private static final ResourceLocation DATA_ID = ResourceLocation.parse("test:data");
    private static final ResourceLocation PERSISTENT_ID = ResourceLocation.parse("test:persistent");
    private static final ResourceLocation SCRIPT_ID = ResourceLocation.parse("test:script");

    @Test
    void rejectsNonPositiveCountAndTimeCost() {
        assertThrows(IllegalArgumentException.class, () -> new Singularity(DATA_ID).setCount(0));
        assertThrows(IllegalArgumentException.class, () -> new Singularity(DATA_ID).setCount(-1));
        assertThrows(IllegalArgumentException.class, () -> new Singularity(DATA_ID).setTimeCost(0));
        assertThrows(IllegalArgumentException.class, () -> new Singularity(DATA_ID).setTimeCost(-1));
    }

    @Test
    void rebuildsScriptStateButKeepsPersistentRegistrations() {
        SingularityReloadListener listener = new SingularityReloadListener();
        listener.beginReload(Map.of(DATA_ID, new Singularity(DATA_ID)));
        listener.registerPersistentSingularity(new Singularity(PERSISTENT_ID));
        listener.registerScriptSingularity(new Singularity(SCRIPT_ID));
        listener.removeSingularity(DATA_ID);
        listener.commitReload();

        assertEquals(2, listener.getAllSingularities().size());
        assertNull(listener.getSingularity(DATA_ID));
        assertNotNull(listener.getSingularity(PERSISTENT_ID));
        assertNotNull(listener.getSingularity(SCRIPT_ID));

        Singularity returnedCopy = listener.getSingularity(PERSISTENT_ID);
        returnedCopy.setCount(2);
        assertEquals(1000, listener.getSingularity(PERSISTENT_ID).getRealCount());

        listener.beginReload(Map.of());
        listener.commitReload();
        assertEquals(1, listener.getAllSingularities().size());
        assertNotNull(listener.getSingularity(PERSISTENT_ID));
        assertNull(listener.getSingularity(SCRIPT_ID));
    }

    @Test
    void replacesDatapackSnapshotAndDoesNotMutateSourcesWhenFilteringRecipes() {
        SingularityReloadListener listener = new SingularityReloadListener();
        listener.beginReload(Map.of(DATA_ID, new Singularity(DATA_ID)));
        listener.removeSingularityRecipe(DATA_ID);
        listener.commitReload();

        assertFalse(listener.getSingularity(DATA_ID).isRecipeEnabled());
        assertTrue(listener.getDataSingularities().get(DATA_ID).isRecipeEnabled());

        listener.beginReload(Map.of(SCRIPT_ID, new Singularity(SCRIPT_ID)));
        listener.commitReload();
        assertNull(listener.getSingularity(DATA_ID));
        assertNotNull(listener.getSingularity(SCRIPT_ID));
    }

    @Test
    void removeAllAndClientReplacementUseDefensiveEffectiveSnapshots() {
        SingularityReloadListener listener = new SingularityReloadListener();
        listener.beginReload(Map.of(DATA_ID, new Singularity(DATA_ID)));
        listener.setRemoveAll(true);
        listener.commitReload();
        assertTrue(listener.getAllSingularities().isEmpty());
        assertNull(listener.getSingularity(DATA_ID));

        Singularity synchronizedSingularity = new Singularity(SCRIPT_ID);
        listener.replaceEffectiveSnapshot(List.of(synchronizedSingularity));
        synchronizedSingularity.setCount(2);
        assertEquals(1000, listener.getSingularity(SCRIPT_ID).getRealCount());

        listener.replaceEffectiveSnapshot(List.of());
        assertTrue(listener.getAllSingularities().isEmpty());
    }

    @Test
    void normalizesHistoricalFieldsAndColors() {
        JsonObject historical = new JsonObject();
        historical.addProperty("name", "test:historical");
        historical.addProperty("timeRequired", 80);
        historical.addProperty("recipeDisabled", true);
        historical.addProperty("overlayColor", "e1e1e1");
        historical.addProperty("underlayColor", "6c6c6c");

        JsonElement normalizedElement = SingularityReloadListener.normalizeLegacyJson(historical, DATA_ID);
        JsonObject normalized = normalizedElement.getAsJsonObject();
        assertEquals(80, normalized.get("timeCost").getAsInt());
        assertTrue(normalized.get("recipeEnabled").getAsBoolean());
        assertEquals(0xE1E1E1, normalized.get("overlayColor").getAsInt());
        assertEquals(0x6C6C6C, normalized.get("underlayColor").getAsInt());
        assertFalse(normalized.has("timeRequired"));
        assertFalse(normalized.has("recipeDisabled"));
    }

    @Test
    void resolvesMissingCodecTimeCostAtDecodeTime() {
        AtomicInteger defaultCalls = new AtomicInteger();
        Codec<Singularity> codec = Singularity.createCodec(() -> {
            defaultCalls.incrementAndGet();
            return 321;
        });
        JsonObject json = new JsonObject();
        json.addProperty("name", "test:codec_default");
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", "minecraft:iron_ingot");
        json.add("ingredient", ingredient);

        assertEquals(0, defaultCalls.get());
        Singularity withDefault = codec.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(321, withDefault.getTimeCost());
        assertEquals(1, defaultCalls.get());

        json.addProperty("timeCost", 120);
        Singularity withExplicitValue = codec.parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(120, withExplicitValue.getTimeCost());
        JsonObject encodedExplicitValue = codec.encodeStart(JsonOps.INSTANCE, withExplicitValue).getOrThrow().getAsJsonObject();
        assertEquals(120, encodedExplicitValue.get("timeCost").getAsInt());

        json.addProperty("timeCost", 240);
        Singularity withSpecDefault = codec.parse(JsonOps.INSTANCE, json).getOrThrow();
        JsonObject encodedSpecDefault = codec.encodeStart(JsonOps.INSTANCE, withSpecDefault).getOrThrow().getAsJsonObject();
        assertFalse(encodedSpecDefault.has("timeCost"));
        assertEquals(1, defaultCalls.get());
    }

    @Test
    void validatesMapIdsAndProvidesCompatibleBuilderMethods() {
        Singularity singularity = new Singularity(DATA_ID);
        assertEquals(DATA_ID.toString(), singularity.getDisplayName());
        assertFalse(singularity.setRecipeDisabled(true).isRecipeEnabled());
        assertThrows(UnsupportedOperationException.class, () -> singularity.getConditions().clear());
        assertThrows(IllegalArgumentException.class,
                () -> new SingularityReloadListener().beginReload(Map.of(SCRIPT_ID, singularity)));
    }
}
