package committee.nova.mods.avaritia.init.data.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaritiaCompatRecipeProviderTest {
    private static final Path RECIPE_DIR = Path.of("src/generated/resources/data/avaritia/recipe");

    @Test
    void recentOptionalRecipesKeepTheirModConditionsAndOutputs() throws IOException {
        Map<String, String> expected = Map.of(
                "botania_mana_tablet", "botania:mana_tablet",
                "botania_creative_pool", "botania:creative_pool",
                "mek_creative_energy_cube", "mekanism:creative_energy_cube",
                "tc3_creative_slot_ability", "tconstruct:creative_slot",
                "tc3_creative_slot_defense", "tconstruct:creative_slot",
                "tc3_creative_slot_souls", "tconstruct:creative_slot",
                "tc3_creative_slot_upgrades", "tconstruct:creative_slot"
        );

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            JsonObject recipe = read(entry.getKey());
            assertEquals("avaritia:shaped_table", recipe.get("type").getAsString());
            assertEquals(entry.getValue(), recipe.getAsJsonObject("result").get("id").getAsString());
            assertEquals(1, recipe.getAsJsonArray("neoforge:conditions").size());
            assertEquals("neoforge:mod_loaded", recipe.getAsJsonArray("neoforge:conditions")
                    .get(0).getAsJsonObject().get("type").getAsString());
        }
    }

    @Test
    void optionalOutputsUseTargetVersionComponents() throws IOException {
        JsonObject botaniaComponents = read("botania_mana_tablet")
                .getAsJsonObject("result").getAsJsonObject("components");
        JsonObject botaniaData = botaniaComponents.getAsJsonObject("minecraft:custom_data");
        assertEquals(500_000, botaniaData.get("mana").getAsInt());
        assertTrue(botaniaData.get("creative").getAsBoolean());

        JsonObject mekanismComponents = read("mek_creative_energy_cube")
                .getAsJsonObject("result").getAsJsonObject("components");
        assertEquals(Long.MAX_VALUE, mekanismComponents.getAsJsonObject("mekanism:energy")
                .getAsJsonArray("energy_containers").get(0).getAsLong());

        assertEquals("abilities", creativeSlot("ability"));
        assertEquals("defense", creativeSlot("defense"));
        assertEquals("souls", creativeSlot("souls"));
        assertEquals("upgrades", creativeSlot("upgrades"));
    }

    private static String creativeSlot(String recipeSuffix) throws IOException {
        return read("tc3_creative_slot_" + recipeSuffix)
                .getAsJsonObject("result")
                .getAsJsonObject("components")
                .getAsJsonObject("minecraft:custom_data")
                .get("slot").getAsString();
    }

    private static JsonObject read(String id) throws IOException {
        return JsonParser.parseString(Files.readString(RECIPE_DIR.resolve(id + ".json"))).getAsJsonObject();
    }
}
