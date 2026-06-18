package committee.nova.mods.avaritia.util;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SingularityUtilsTest {
    @Test
    void readsTimeCostBeforeLegacyTimeRequired() {
        JsonObject json = new JsonObject();
        json.addProperty("timeRequired", 20);
        json.addProperty("timeCost", 40);

        assertThat(SingularityUtils.getTimeCost(json, 240)).isEqualTo(40);
    }

    @Test
    void readsLegacyTimeRequiredWhenTimeCostIsMissing() {
        JsonObject json = new JsonObject();
        json.addProperty("timeRequired", 25);

        assertThat(SingularityUtils.getTimeCost(json, 240)).isEqualTo(25);
    }

    @Test
    void fallsBackToDefaultTimeCost() {
        JsonObject json = new JsonObject();

        assertThat(SingularityUtils.getTimeCost(json, 240)).isEqualTo(240);
    }

    @Test
    void readsCanonicalRecipeEnabledBeforeLegacyRecipeDisabled() {
        JsonObject json = new JsonObject();
        json.addProperty("recipeDisabled", true);
        json.addProperty("recipeEnabled", false);

        assertThat(SingularityUtils.getRecipeEnabled(json)).isFalse();
    }

    @Test
    void preservesLegacyRecipeDisabledAsRecipeEnabled() {
        JsonObject json = new JsonObject();
        json.addProperty("recipeDisabled", true);

        assertThat(SingularityUtils.getRecipeEnabled(json)).isTrue();
    }

    @Test
    void defaultsRecipeEnabledWhenRecipeFieldIsMissing() {
        JsonObject json = new JsonObject();

        assertThat(SingularityUtils.getRecipeEnabled(json)).isTrue();
    }
}
