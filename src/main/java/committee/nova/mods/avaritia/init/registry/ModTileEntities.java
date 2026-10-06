package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.tile.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import committee.nova.mods.avaritia.api.init.registry.RegistryEntry;

import java.util.function.Supplier;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 9:48
 * Version: 1.0
 */
public class ModTileEntities {
    public static final RegistryEntries<BlockEntityType<?>> BLOCK_ENTITIES = RegistryEntries.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Const.MOD_ID);

    public static <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(null));
    }


    public static RegistryEntry<BlockEntityType<NeutronCollectorTile>> neutron_collector_tile = blockEntity(
            "neutron_collector_tile",
            NeutronCollectorTile::new,
            () -> new Block[]{
                    ModBlocks.neutron_collector.get(),
                    ModBlocks.dense_neutron_collector.get(),
                    ModBlocks.denser_neutron_collector.get(),
                    ModBlocks.densest_neutron_collector.get()
            });
    public static RegistryEntry<BlockEntityType<NeutronCompressorTile>> neutron_compressor_tile = blockEntity(
            "neutron_compressor_tile",
            NeutronCompressorTile::new,
            () -> new Block[]{
                    ModBlocks.neutron_compressor.get(),
                    ModBlocks.dense_neutron_compressor.get(),
                    ModBlocks.denser_neutron_compressor.get(),
                    ModBlocks.densest_neutron_compressor.get()
            });
    public static RegistryEntry<BlockEntityType<TierCraftTile>> mod_craft_tile = blockEntity("mod_craft_tile", TierCraftTile::new,
            () -> new Block[]{
                    ModBlocks.sculk_crafting_table.get(),
                    ModBlocks.nether_crafting_table.get(),
                    ModBlocks.end_crafting_table.get(),
                    ModBlocks.extreme_crafting_table.get()//超立方体
            });
    public static RegistryEntry<BlockEntityType<CompressedChestTile>> compressed_chest_tile = blockEntity("compressed_chest_tile", CompressedChestTile::new, () -> new Block[]{ModBlocks.compressed_chest.get()});
    public static RegistryEntry<BlockEntityType<TesseractTile>> tesseract_tile = blockEntity("tesseract_tile", TesseractTile::new, () -> new Block[]{ModBlocks.tesseract.get()});
    public static RegistryEntry<BlockEntityType<InfinityChestTile>> infinity_chest_tile = blockEntity("infinity_chest_tile", InfinityChestTile::new, () -> new Block[]{ModBlocks.infinity_chest.get()});
}
