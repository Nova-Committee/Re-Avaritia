package committee.nova.mods.avaritia.core.singularity;

import com.google.gson.JsonObject;
import committee.nova.mods.avaritia.util.SingularityUtils;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
    void readsCanonicalAndHistoricalJsonFieldsAndWritesOnlyCanonicalFields() {
        JsonObject canonical = baseJson("test:canonical");
        canonical.addProperty("timeCost", 120);
        canonical.addProperty("recipeEnabled", false);
        Singularity parsedCanonical = SingularityUtils.loadFromJson(canonical);
        assertEquals(120, parsedCanonical.getTimeCost());
        assertFalse(parsedCanonical.isRecipeEnabled());

        JsonObject historical = baseJson("test:historical");
        historical.addProperty("timeRequired", 80);
        historical.addProperty("recipeDisabled", true);
        Singularity parsedHistorical = SingularityUtils.loadFromJson(historical);
        assertEquals(80, parsedHistorical.getTimeCost());
        assertTrue(parsedHistorical.isRecipeEnabled());

        JsonObject written = SingularityUtils.writeToJson(parsedHistorical);
        assertTrue(written.has("timeCost"));
        assertTrue(written.has("recipeEnabled"));
        assertFalse(written.has("timeRequired"));
        assertFalse(written.has("recipeDisabled"));

        JsonObject invalid = baseJson("test:invalid");
        invalid.addProperty("count", 0);
        invalid.addProperty("timeCost", 20);
        assertThrows(IllegalArgumentException.class, () -> SingularityUtils.loadFromJson(invalid));
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

    private static JsonObject baseJson(String name) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("displayName", name);
        json.addProperty("overlayColor", "ffffff");
        json.addProperty("underlayColor", "000000");
        json.addProperty("count", 1000);
        return json;
    }
}
