package committee.nova.mods.avaritia.common.item;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolAndItemParityTest {
    private static final Path MAIN_SOURCES = Path.of("src/main/java/committee/nova/mods/avaritia");
    private static final Pattern TOOL_SPEED = Pattern.compile("ModToolTiers\\.(BLAZE|CRYSTAL|INFINITY)\\.speed\\(\\)");

    @Test
    void eachToolFamilyUsesItsOwnMaterialSpeed() throws IOException {
        Map<String, String> families = Map.of(
                "blaze", "BLAZE",
                "crystal", "CRYSTAL",
                "infinity", "INFINITY"
        );

        for (var family : families.entrySet()) {
            Path directory = MAIN_SOURCES.resolve("common/item/tools/" + family.getKey());
            int speedReferences = 0;
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.filter(file -> file.toString().endsWith(".java")).toList()) {
                    var matcher = TOOL_SPEED.matcher(Files.readString(path));
                    while (matcher.find()) {
                        speedReferences++;
                        assertEquals(family.getValue(), matcher.group(1),
                                () -> path + " must use its own tool material speed");
                    }
                }
            }
            assertTrue(speedReferences >= 5,
                    family.getKey() + " should define speed for at least its five mining/melee tools");
        }
    }

    @Test
    void migratedMaterialStatsMatchTheReferenceBranch() throws IOException {
        String source = compact(read("init/registry/ModToolTiers.java"));

        assertAll(
                () -> assertTrue(source.contains("BLAZE=newToolMaterial(ModTags.INCORRECT_FOR_BLAZE_TOOL,7777,25f,25f,77")),
                () -> assertTrue(source.contains("CRYSTAL=newToolMaterial(ModTags.INCORRECT_FOR_CRYSTAL_TOOL,8888,50f,50f,888")),
                () -> assertTrue(source.contains("INFINITY=newToolMaterial(ModTags.INCORRECT_FOR_INFINITY_TOOL,9999,100f,100f,9999"))
        );
    }

    @Test
    void migratedItemLimitsAndInitialEnchantmentsMatchTheReferenceBranch() throws IOException {
        String eternalSingularity = compact(read("common/item/singularity/EternalSingularityItem.java"));
        String horseArmor = compact(read("common/item/misc/NeutronHorseArmorItem.java"));

        assertAll(
                () -> assertTrue(eternalSingularity.contains("stacksTo(16)")),
                () -> assertFalse(eternalSingularity.contains("stacksTo(8)")),
                () -> assertTrue(horseArmor.contains("newInitEnchantment(Enchantments.FEATHER_FALLING,10)")),
                () -> assertTrue(horseArmor.contains("enchantmentHolder.is(Enchantments.FEATHER_FALLING)){return10;"))
        );
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(MAIN_SOURCES.resolve(relativePath));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
