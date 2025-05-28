package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.render.tile.CompressedChestRenderer;
import committee.nova.mods.avaritia.common.tile.*;
import committee.nova.mods.avaritia.common.tile.collector.BaseNeutronCollectorTile;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2022/3/31 11:37
 * @Description:
 */
public class ModTileEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Const.MOD_ID);

    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(null));
    }

    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {
        BlockEntityRenderers.register(compressed_chest_tile.get(), CompressedChestRenderer::new);
    }

    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<BaseNeutronCollectorTile>> neutron_collector_tile = blockEntity(
            "neutron_collector_tile",
            (BaseNeutronCollectorTile::new),
            () -> new Block[]{
                    ModBlocks.neutron_collector.get(),
                    ModBlocks.dense_neutron_collector.get(),
                    ModBlocks.denser_neutron_collector.get(),
                    ModBlocks.densest_neutron_collector.get()
            }
    );
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<CompressorTile>> compressor_tile = blockEntity("compressor_tile", CompressorTile::new, () -> new Block[]{ModBlocks.neutron_compressor.get()});
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<TierCraftTile>> mod_craft_tile = blockEntity("mod_craft_tile", TierCraftTile::new,
            () -> new Block[]{
                    ModBlocks.sculk_crafting_table.get(),
                    ModBlocks.nether_crafting_table.get(),
                    ModBlocks.end_crafting_table.get(),
                    ModBlocks.extreme_crafting_table.get()
            });
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<CompressedChestTile>> compressed_chest_tile = blockEntity("compressed_chest_tile", CompressedChestTile::new, () -> new Block[]{ModBlocks.compressed_chest.get()});
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<InfinityChestTile>> infinity_chest_tile = blockEntity("infinity_chest_tile", InfinityChestTile::new, () -> new Block[]{ModBlocks.infinity_chest.get()});
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<InfinityClockTile>> infinity_clock_tile = blockEntity("infinity_clock_tile", InfinityClockTile::new, () -> new Block[]{ModBlocks.infinity_clock.get()});

}
