package committee.nova.mods.avaritia.api.common.block;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseBlockTest {
    private static final Path MAIN_SOURCES = Path.of("src/main/java/committee/nova/mods/avaritia");

    @Test
    void blockStrengthValuesMatchAvaritia121Sources() {
        assertAll(
                contains("api/common/block/BaseBlock.java",
                        "properties.sound(sound).strength(hardness, resistance).mapColor(color).requiresCorrectToolForDrops()"),
                contains("api/common/block/BaseBlock.java",
                        "properties.sound(sound).strength(hardness, resistance).mapColor(color)"),
                contains("init/registry/enums/ModResourceBlocks.java", "BLAZE(50, 1000, 1000, 9)"),
                contains("init/registry/enums/ModResourceBlocks.java", "CRYSTAL(100, 2000, 2000, 11)"),
                contains("init/registry/enums/ModResourceBlocks.java", "NEUTRON(8888, 8888, 8888, 13)"),
                contains("init/registry/enums/ModResourceBlocks.java", "INFINITY(9999, 9999, 9999, 15)"),
                contains("init/registry/enums/ModCraftTier.java",
                        "SCULK(\"sculk_crafting_table\", SoundType.SCULK_CATALYST, 25, 500"),
                contains("init/registry/enums/ModCraftTier.java",
                        "NETHER(\"nether_crafting_table\", SoundType.NETHERRACK, 50, 1000"),
                contains("init/registry/enums/ModCraftTier.java",
                        "END(\"end_crafting_table\", ModSounds.END_PORTAL, 75, 1500"),
                contains("init/registry/enums/ModCraftTier.java",
                        "EXTREME(\"extreme_crafting_table\", SoundType.GLASS, 100, 2000"),
                contains("common/block/craft/CompressedCraftTableBlock.java",
                        "super(MapColor.WOOD, SoundType.WOOD, 5F, 100F, true)"),
                contains("common/block/craft/DoubleCompressedCraftTableBlock.java",
                        "super(MapColor.WOOD, SoundType.WOOD, 20F, 500F, true)"),
                contains("common/block/collector/NeutronCollectorBlock.java",
                        "super(MapColor.METAL, SoundType.METAL, 50f, 2000f)"),
                contains("common/block/compressor/NeutronCompressorBlock.java",
                        "super(MapColor.METAL, SoundType.METAL, 50F, 2000F, true)"),
                contains("common/block/chest/CompressedChestBlock.java",
                        ".strength(2.5F).sound(SoundType.WOOD)"),
                contains("common/block/chest/InfinityChestBlock.java",
                        ".strength(2.5F).sound(SoundType.GLASS)"),
                contains("common/block/extreme/ExtremeAnvilBlock.java",
                        ".strength(ModResourceBlocks.NEUTRON.hardness, ModResourceBlocks.NEUTRON.resistance)"),
                contains("common/block/extreme/ExtremeSmithingTableBlock.java",
                        ".strength(2.5F).sound(SoundType.GLASS));"),
                doesNotContain("common/block/extreme/ExtremeSmithingTableBlock.java", ".instabreak()"),
                contains("common/block/misc/SoulFarmLandBlock.java", ".strength(0.6F)"),
                contains("common/block/cake/EndlessCakeBlock.java", ".strength(0.5F)"),
                contains("init/registry/ModBlocks.java",
                        "diamond_lattice_block = itemBlock(\"diamond_lattice_block\", () -> new Block(properties().strength(100F, 100F)"),
                contains("init/registry/ModBlocks.java",
                        "star_fuel_block = itemBurnBlock(\"star_fuel_block\", () -> new Block(properties().strength(100F, 200F)"),
                contains("init/registry/ModBlocks.java",
                        "refined_coal_block = itemBurnBlock(\"refined_coal_block\", () -> new Block(properties().strength(50F, 50F)"),
                contains("init/registry/ModBlocks.java", ".strength(1000F, 3600000.0F)"),
                occursAtLeast("init/registry/ModBlocks.java", ".strength(400F, 3600000.0F)", 2)
        );
    }

    private static Executable contains(String relativePath, String expected) {
        return () -> assertTrue(
                compact(read(relativePath)).contains(compact(expected)),
                () -> relativePath + " should keep migrated block strength: " + expected
        );
    }

    private static Executable doesNotContain(String relativePath, String unexpected) {
        return () -> assertFalse(
                compact(read(relativePath)).contains(compact(unexpected)),
                () -> relativePath + " should not override migrated block strength: " + unexpected
        );
    }

    private static Executable occursAtLeast(String relativePath, String expected, int minimum) {
        return () -> {
            String source = compact(read(relativePath));
            String pattern = compact(expected);
            int count = 0;
            int from = 0;
            while ((from = source.indexOf(pattern, from)) >= 0) {
                count++;
                from += pattern.length();
            }
            assertTrue(count >= minimum, () -> relativePath + " should keep at least " + minimum
                    + " migrated block strength entries: " + expected);
        };
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(MAIN_SOURCES.resolve(relativePath));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
