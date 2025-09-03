package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.render.tile.BlackHoleChestRender;
import committee.nova.mods.avaritia.client.render.tile.CompressedChestRenderer;
import committee.nova.mods.avaritia.common.tile.*;
import dev.architectury.registry.registries.DeferredRegister;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
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
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Const.MOD_ID);

    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(null));
    }

    @Environment(EnvType.CLIENT)
    public static void onClientSetup() {
        BlockEntityRenderers.register(compressed_chest_tile.get(), CompressedChestRenderer::new);
        BlockEntityRenderers.register(black_hole_chest_tile.get(), BlackHoleChestRender::new);
    }

    public static RegistryObject<BlockEntityType<NeutronCollectorTile>> neutron_collector_tile = blockEntity(
            "neutron_collector_tile",
            NeutronCollectorTile::new,
            () -> new Block[]{
                    ModBlocks.neutron_collector.get(),
                    ModBlocks.dense_neutron_collector.get(),
                    ModBlocks.denser_neutron_collector.get(),
                    ModBlocks.densest_neutron_collector.get()
            });
    public static RegistryObject<BlockEntityType<NeutronCompressorTile>> neutron_compressor_tile = blockEntity(
            "neutron_compressor_tile",
            NeutronCompressorTile::new,
            () -> new Block[]{
                    ModBlocks.neutron_compressor.get(),
                    ModBlocks.dense_neutron_compressor.get(),
                    ModBlocks.denser_neutron_compressor.get(),
                    ModBlocks.densest_neutron_compressor.get()
            });
    public static RegistryObject<BlockEntityType<TierCraftTile>> mod_craft_tile = blockEntity("mod_craft_tile", TierCraftTile::new,
            () -> new Block[]{
                    ModBlocks.sculk_crafting_table.get(),
                    ModBlocks.nether_crafting_table.get(),
                    ModBlocks.end_crafting_table.get(),
                    ModBlocks.extreme_crafting_table.get()//超立方体
            });
    public static RegistryObject<BlockEntityType<CompressedChestTile>> compressed_chest_tile = blockEntity("compressed_chest_tile", CompressedChestTile::new, () -> new Block[]{ModBlocks.compressed_chest.get()});
    public static RegistryObject<BlockEntityType<InfinityChestTile>> infinity_chest_tile = blockEntity("infinity_chest_tile", InfinityChestTile::new, () -> new Block[]{ModBlocks.infinity_chest.get()});
    //    public static RegistryObject<BlockEntityType<InfinityClockTile>> infinity_clock_tile = blockEntity("infinity_clock_tile", InfinityClockTile::new, () -> new Block[]{ModBlocks.infinity_clock.json.get()});
    public static RegistryObject<BlockEntityType<BlackHoleChestTile>> black_hole_chest_tile = blockEntity("black_hole_chest_tile", BlackHoleChestTile::new, () -> new Block[]{ModBlocks.black_hole_chest.get()});

}
