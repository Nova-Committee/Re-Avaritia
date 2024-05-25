package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.tile.CompressorTile;
import committee.nova.mods.avaritia.common.tile.ExtremeCraftingTile;
import committee.nova.mods.avaritia.common.tile.collector.*;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:48
 * Version: 1.0
 */
public class ModTileEntities {
    public static final LazyRegistrar<BlockEntityType<?>> BLOCK_ENTITIES = LazyRegistrar.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Static.MOD_ID);
    public static void init() {
        Static.LOGGER.info("Registering Mod Block Entities...");
        BLOCK_ENTITIES.register();
    }

    //public static Supplier<BlockEntityType<InfinitatoTile>> infinitato_tile = blockEntity("infinitato_tile", InfinitatoTile::new, () -> new Block[]{ModBlocks.infinitato.get()});

    public static RegistryObject<BlockEntityType<AbsNeutronCollectorTile>> neutron_collector_tile = blockEntity("neutron_collector_tile", DefaultNeutronCollectorTile::new, () -> new Block[]{ModBlocks.neutron_collector.get()});
    public static RegistryObject<BlockEntityType<AbsNeutronCollectorTile>> dense_neutron_collector_tile = blockEntity("dense_neutron_collector_tile", DenseNeutronCollectorTile::new, () -> new Block[]{ModBlocks.dense_neutron_collector.get()});
    public static RegistryObject<BlockEntityType<AbsNeutronCollectorTile>> denser_neutron_collector_tile = blockEntity("denser_neutron_collector_tile", DenserNeutronCollectorTile::new, () -> new Block[]{ModBlocks.denser_neutron_collector.get()});
    public static RegistryObject<BlockEntityType<AbsNeutronCollectorTile>> densest_neutron_collector_tile = blockEntity("densest_neutron_collector_tile", DensestNeutronCollectorTile::new, () -> new Block[]{ModBlocks.densest_neutron_collector.get()});
    public static RegistryObject<BlockEntityType<CompressorTile>> compressor_tile = blockEntity("compressor_tile", CompressorTile::new, () -> new Block[]{ModBlocks.neutron_compressor.get()});
    public static RegistryObject<BlockEntityType<ExtremeCraftingTile>> extreme_crafting_tile = blockEntity("extreme_crafting_tile", ExtremeCraftingTile::new, () -> new Block[]{ModBlocks.extreme_crafting_table.get()});

    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(null));
    }

    @Environment(EnvType.CLIENT)
    public static void onClientSetup() {

    }
}
