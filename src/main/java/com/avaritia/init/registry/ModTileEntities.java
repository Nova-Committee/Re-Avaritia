package com.avaritia.init.registry;

import com.avaritia.Const;
import com.avaritia.common.tile.CompressedChestTile;
import com.avaritia.common.tile.InfinityChestTile;
import com.avaritia.common.tile.NeutronCollectorTile;
import com.avaritia.common.tile.NeutronCompressorTile;
import com.avaritia.common.tile.TierCraftTile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

/**
 * 注册模组中的所有方块实体类型。
 */
public class ModTileEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Const.MOD_ID);

    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return BLOCK_ENTITIES.register(name, () -> new BlockEntityType<>(tile, Set.of(blocks.get())));
    }

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<NeutronCollectorTile>> neutron_collector_tile = blockEntity(
            "neutron_collector_tile",
            NeutronCollectorTile::new,
            () -> new Block[]{
                    ModBlocks.neutron_collector.get(),
                    ModBlocks.dense_neutron_collector.get(),
                    ModBlocks.denser_neutron_collector.get(),
                    ModBlocks.densest_neutron_collector.get()
            }
    );
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<NeutronCompressorTile>> neutron_compressor_tile = blockEntity(
            "neutron_compressor_tile",
            NeutronCompressorTile::new,
            () -> new Block[]{
                    ModBlocks.neutron_compressor.get(),
                    ModBlocks.dense_neutron_compressor.get(),
                    ModBlocks.denser_neutron_compressor.get(),
                    ModBlocks.densest_neutron_compressor.get()
            }
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfinityChestTile>> INFINITY_CHEST_TILE = blockEntity(
            "infinity_chest_tile",
            InfinityChestTile::new,
            () -> new Block[]{ModBlocks.infinity_chest.get()}
    );

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TierCraftTile>> mod_craft_tile = blockEntity("mod_craft_tile", TierCraftTile::new,
            () -> new Block[]{
                    ModBlocks.sculk_crafting_table.get(),
                    ModBlocks.nether_crafting_table.get(),
                    ModBlocks.end_crafting_table.get(),
                    ModBlocks.extreme_crafting_table.get()
            });
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<CompressedChestTile>> compressed_chest_tile = blockEntity("compressed_chest_tile", CompressedChestTile::new, () -> new Block[]{ModBlocks.compressed_chest.get()});

}
