package committee.nova.mods.avaritia.common.block;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisteredBlockAndItemParityTest {
    private static final Path MOD_ITEMS = Path.of("src/main/java/committee/nova/mods/avaritia/init/registry/ModItems.java");
    private static final Path MOD_BLOCKS = Path.of("src/main/java/committee/nova/mods/avaritia/init/registry/ModBlocks.java");
    private static final Path GENERATED = Path.of("src/generated/resources");
    private static final Pattern ITEM_REGISTRATION = Pattern.compile("\\bitem\\(\\s*\"([^\"]+)\"");
    private static final Pattern BLOCK_REGISTRATION = Pattern.compile("\\b(?:itemBlock|itemBurnBlock|baseBlock)\\(\\s*\"([^\"]+)\"");
    private static final Set<String> ITEMLESS_BLOCKS = Set.of("fake_bedrock", "fake_end_portal", "fake_end_portal_frame");

    @Test
    void everyRegisteredItemAndBlockHasGeneratedClientResources() throws IOException {
        Set<String> explicitItems = registrations(MOD_ITEMS, ITEM_REGISTRATION);
        Set<String> blocks = registrations(MOD_BLOCKS, BLOCK_REGISTRATION);
        Set<String> expectedItemModels = new HashSet<>(explicitItems);
        expectedItemModels.addAll(blocks.stream().filter(block -> !ITEMLESS_BLOCKS.contains(block)).toList());

        Set<String> itemModels = jsonNames(GENERATED.resolve("assets/avaritia/items"));
        Set<String> blockStates = jsonNames(GENERATED.resolve("assets/avaritia/blockstates"));

        assertAll(
                () -> assertEquals(59, explicitItems.size(), "explicit item registry inventory changed unexpectedly"),
                () -> assertEquals(30, blocks.size(), "block registry inventory changed unexpectedly"),
                () -> assertEquals(expectedItemModels, itemModels, "registered item/block item models must be complete"),
                () -> assertEquals(blocks, blockStates, "every registered block must have a blockstate")
        );
    }

    @Test
    void everyRegisteredBlockHasALootTable() throws IOException {
        Set<String> blocks = registrations(MOD_BLOCKS, BLOCK_REGISTRATION);
        Set<String> lootTables = jsonNames(GENERATED.resolve("data/avaritia/loot_table/blocks"));

        assertEquals(blocks, lootTables, "every registered block must declare its drop behavior");
    }

    @Test
    void storageAndMachineBlocksKeepTheirDataRemovalContracts() throws IOException {
        String compressedChest = compact(readMain("common/block/chest/CompressedChestBlock.java"));
        String infinityChest = compact(readMain("common/block/chest/InfinityChestBlock.java"));
        String inventoryTile = compact(readMain("api/common/tile/BaseInventoryTileEntity.java"));
        String collector = compact(readMain("common/block/collector/NeutronCollectorBlock.java"));

        assertAll(
                () -> assertTrue(compressedChest.contains("pStack.applyComponents(chestTile.collectComponents());")),
                () -> assertTrue(compressedChest.contains("itemstack.applyComponents(chestTile.collectComponents())")),
                () -> assertTrue(infinityChest.contains("dropStack.set(ModDataComponents.CLUSTER_CONTAINER,ClusterContainerContents.fromItems(tile.chest.getItems()));")),
                () -> assertTrue(infinityChest.contains("contents.copyInto(tile.chest.getItems());")),
                () -> assertTrue(inventoryTile.contains("Containers.dropContents(level,pos,this.getInventory().getStacks());")),
                () -> assertTrue(collector.contains("super(MapColor.METAL,SoundType.METAL,50f,2000f,true);"))
        );
    }

    private static Set<String> registrations(Path source, Pattern pattern) throws IOException {
        var matcher = pattern.matcher(Files.readString(source));
        Set<String> names = new HashSet<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private static Set<String> jsonNames(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            return paths
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString().replaceFirst("\\.json$", ""))
                    .collect(Collectors.toSet());
        }
    }

    private static String readMain(String relativePath) throws IOException {
        return Files.readString(Path.of("src/main/java/committee/nova/mods/avaritia").resolve(relativePath));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
