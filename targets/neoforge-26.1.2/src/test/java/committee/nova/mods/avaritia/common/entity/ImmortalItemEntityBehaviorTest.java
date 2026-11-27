package committee.nova.mods.avaritia.common.entity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImmortalItemEntityBehaviorTest {
    private static final Path MAIN_SOURCES = Path.of("src/main/java/committee/nova/mods/avaritia");
    private static final Path ENDLESS_TAG = Path.of("src/generated/resources/data/avaritia/tags/item/endless.json");
    private static final Pattern COORDINATE_FACTORY = Pattern.compile(
            "ImmortalItemEntity\\.create\\([^;]*(?:getX|getY|getZ)\\(", Pattern.DOTALL
    );

    @Test
    void replacementEntityPreservesAndLocksTheOriginalOwner() throws IOException {
        String source = compact(read("common/entity/ImmortalItemEntity.java"));

        assertAll(
                () -> assertTrue(source.contains("entity.restoreFrom(location);")),
                () -> assertTrue(source.contains("entity.lockedReturnPlayerId=getLockedReturnPlayerId(location);")),
                () -> assertTrue(source.contains("UUIDtarget=itemEntity.getTarget();")),
                () -> assertTrue(source.contains("Entityowner=itemEntity.getOwner();")),
                () -> assertTrue(source.contains("this.setTarget(this.lockedReturnPlayerId);")),
                () -> assertTrue(source.contains("serverLevel.getPlayerByUUID(this.lockedReturnPlayerId)"))
        );
    }

    @Test
    void fullInventoryNeverDiscardsTheRemainingStack() throws IOException {
        String source = compact(read("common/entity/ImmortalItemEntity.java"));
        int insert = source.indexOf("player.getInventory().add(remaining);");
        int emptyCheck = source.indexOf("if(remaining.isEmpty())", insert);
        int discard = source.indexOf("this.discard();", emptyCheck);
        int keepRemaining = source.indexOf("this.setItem(remaining);", discard);

        assertAll(
                () -> assertTrue(insert >= 0, "return path should attempt inventory insertion"),
                () -> assertTrue(insert < emptyCheck && emptyCheck < discard,
                        "discard must be guarded by the remaining-stack empty check"),
                () -> assertTrue(discard < keepRemaining,
                        "a partial or failed insertion must keep the remaining stack in the entity"),
                () -> assertFalse(source.contains("player.getInventory().add(this.getItem());this.discard();"))
        );
    }

    @Test
    void everyCustomEntityFactoryPassesTheOriginalDropEntity() throws IOException {
        try (var paths = Files.walk(MAIN_SOURCES)) {
            List<Path> offenders = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.endsWith("ImmortalItemEntity.java"))
                    .filter(path -> {
                        try {
                            return COORDINATE_FACTORY.matcher(Files.readString(path)).find();
                        } catch (IOException exception) {
                            throw new IllegalStateException(exception);
                        }
                    })
                    .toList();

            assertTrue(offenders.isEmpty(), () -> "custom item entities must preserve owner/location metadata: " + offenders);
        }
    }

    @Test
    void baselineImmortalItemsRemainCovered() throws IOException {
        String horseArmor = compact(read("common/item/misc/NeutronHorseArmorItem.java"));
        String tag = Files.readString(ENDLESS_TAG);

        assertAll(
                () -> assertTrue(horseArmor.contains("booleanhasCustomEntity(@NotNullItemStackstack){returntrue;}")),
                () -> assertTrue(horseArmor.contains("ImmortalItemEntity.create(ModEntityTypes.IMMORTAL.get(),level,location,stack)")),
                () -> assertTrue(tag.contains("\"avaritia:singularity\"")),
                () -> assertTrue(tag.contains("\"avaritia:matter_cluster\"")),
                () -> assertTrue(tag.contains("\"avaritia:full_matter_cluster\""))
        );
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(MAIN_SOURCES.resolve(relativePath));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
