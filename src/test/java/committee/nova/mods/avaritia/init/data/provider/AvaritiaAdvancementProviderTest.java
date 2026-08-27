package committee.nova.mods.avaritia.init.data.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaritiaAdvancementProviderTest {
    private static final Path GENERATED_RESOURCES = Path.of("src/generated/resources");
    private static final Path ADVANCEMENT_ROOT = GENERATED_RESOURCES.resolve("data/avaritia/advancement");
    private static final List<String> FOLDERS = List.of("main", "singularity", "infinity");
    private static final List<String> LOCALES = List.of("en_us", "zh_cn", "zh_tw", "ja_jp", "uk_ua");

    @Test
    void rootUsesNeutronGuiSpriteAndEarlyProgressCriterion() throws IOException {
        JsonObject root = read(ADVANCEMENT_ROOT.resolve("main/root.json"));

        assertEquals("avaritia:gui/advancements/backgrounds/neutron",
                root.getAsJsonObject("display").get("background").getAsString());
        assertEquals("avaritia:compressed_crafting_table",
                root.getAsJsonObject("criteria")
                        .getAsJsonObject("main")
                        .getAsJsonObject("conditions")
                        .getAsJsonArray("items")
                        .get(0).getAsJsonObject()
                        .get("items").getAsString());

        JsonObject guiAtlas = read(GENERATED_RESOURCES.resolve("assets/minecraft/atlases/gui.json"));
        JsonArray sources = guiAtlas.getAsJsonArray("sources");
        assertTrue(containsSpriteAlias(sources,
                        "avaritia:block/resource/neutron",
                        "avaritia:gui/advancements/backgrounds/neutron"),
                "GUI atlas must expose the neutron block texture as the advancement background sprite");
    }

    @Test
    void neutronCompressorFollowsNeutronIngot() throws IOException {
        JsonObject compressor = read(ADVANCEMENT_ROOT.resolve("main/neutron_compressor.json"));
        assertEquals("avaritia:main/neutron_ingot", compressor.get("parent").getAsString());
    }

    @Test
    void infinityFoodKeepsEitherFoodRequirement() throws IOException {
        JsonObject food = read(ADVANCEMENT_ROOT.resolve("infinity/infinity_food.json"));
        JsonArray requirements = food.getAsJsonArray("requirements");

        assertEquals(1, requirements.size());
        JsonArray alternatives = requirements.get(0).getAsJsonArray();
        assertEquals(2, alternatives.size());
        assertEquals(Set.of("food0", "food1"), Set.of(
                alternatives.get(0).getAsString(),
                alternatives.get(1).getAsString()
        ));
    }

    @Test
    void generatedAdvancementGraphIsCompleteAcyclicAndTranslated() throws IOException {
        Map<String, JsonObject> advancements = loadAdvancements();
        assertEquals(36, advancements.size());

        for (Map.Entry<String, JsonObject> entry : advancements.entrySet()) {
            String parent = parentOf(entry.getValue());
            if (parent != null) {
                assertTrue(advancements.containsKey(parent),
                        () -> entry.getKey() + " references missing parent " + parent);
            }
            assertAcyclic(entry.getKey(), advancements);
        }

        for (String locale : LOCALES) {
            JsonObject translations = read(GENERATED_RESOURCES.resolve("assets/avaritia/lang/" + locale + ".json"));
            for (Map.Entry<String, JsonObject> entry : advancements.entrySet()) {
                JsonObject display = entry.getValue().getAsJsonObject("display");
                assertNotNull(display, () -> entry.getKey() + " has no display");
                assertTranslationExists(locale, entry.getKey(), "title", display, translations);
                assertTranslationExists(locale, entry.getKey(), "description", display, translations);
            }
        }
    }

    private static boolean containsSpriteAlias(JsonArray sources, String resource, String sprite) {
        for (var element : sources) {
            JsonObject source = element.getAsJsonObject();
            if ("minecraft:single".equals(source.get("type").getAsString())
                    && resource.equals(source.get("resource").getAsString())
                    && sprite.equals(source.get("sprite").getAsString())) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, JsonObject> loadAdvancements() throws IOException {
        Map<String, JsonObject> advancements = new HashMap<>();
        for (String folder : FOLDERS) {
            Path directory = ADVANCEMENT_ROOT.resolve(folder);
            try (var files = Files.list(directory)) {
                for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                    String fileName = file.getFileName().toString();
                    String id = "avaritia:" + folder + "/" + fileName.substring(0, fileName.length() - 5);
                    assertFalse(advancements.containsKey(id), () -> "duplicate advancement id " + id);
                    advancements.put(id, read(file));
                }
            }
        }
        return advancements;
    }

    private static void assertAcyclic(String start, Map<String, JsonObject> advancements) {
        Set<String> path = new HashSet<>();
        String current = start;
        while (current != null) {
            assertTrue(path.add(current), "advancement parent cycle at " + current + " from " + start);
            JsonObject advancement = advancements.get(current);
            current = advancement == null ? null : parentOf(advancement);
        }
    }

    private static String parentOf(JsonObject advancement) {
        return advancement.has("parent") ? advancement.get("parent").getAsString() : null;
    }

    private static void assertTranslationExists(String locale, String advancementId, String field,
                                                JsonObject display, JsonObject translations) {
        String key = display.getAsJsonObject(field).get("translate").getAsString();
        assertTrue(translations.has(key),
                () -> locale + " is missing " + field + " translation " + key + " for " + advancementId);
    }

    private static JsonObject read(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
