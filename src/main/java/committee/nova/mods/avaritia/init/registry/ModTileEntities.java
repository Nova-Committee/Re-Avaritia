package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.render.tile.CompressedChestRenderer;
import committee.nova.mods.avaritia.common.tile.CompressedChestTile;
import committee.nova.mods.avaritia.common.tile.CompressorTile;
import committee.nova.mods.avaritia.common.tile.ExtremeCraftingTile;
import committee.nova.mods.avaritia.common.tile.collector.AbsNeutronCollectorTile;
import committee.nova.mods.avaritia.common.tile.collector.DefaultNeutronCollectorTile;
import committee.nova.mods.avaritia.common.tile.collector.DenseNeutronCollectorTile;
import committee.nova.mods.avaritia.common.tile.collector.DenserNeutronCollectorTile;
import committee.nova.mods.avaritia.common.tile.collector.DensestNeutronCollectorTile;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:48
 * Version: 1.0
 */
public class ModTileEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Static.MOD_ID);

    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(null));
    }

    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {
        BlockEntityRenderers.register(compressed_chest_tile.get(), CompressedChestRenderer::new);
    }

    public static RegistryObject<BlockEntityType<AbsNeutronCollectorTile>> neutron_collector_tile = blockEntity("neutron_collector_tile", DefaultNeutronCollectorTile::new, () -> new Block[]{ModBlocks.neutron_collector.get()});
    public static RegistryObject<BlockEntityType<AbsNeutronCollectorTile>> dense_neutron_collector_tile = blockEntity("dense_neutron_collector_tile", DenseNeutronCollectorTile::new, () -> new Block[]{ModBlocks.dense_neutron_collector.get()});
    public static RegistryObject<BlockEntityType<AbsNeutronCollectorTile>> denser_neutron_collector_tile = blockEntity("denser_neutron_collector_tile", DenserNeutronCollectorTile::new, () -> new Block[]{ModBlocks.denser_neutron_collector.get()});
    public static RegistryObject<BlockEntityType<AbsNeutronCollectorTile>> densest_neutron_collector_tile = blockEntity("densest_neutron_collector_tile", DensestNeutronCollectorTile::new, () -> new Block[]{ModBlocks.densest_neutron_collector.get()});
    public static RegistryObject<BlockEntityType<CompressorTile>> compressor_tile = blockEntity("compressor_tile", CompressorTile::new, () -> new Block[]{ModBlocks.neutron_compressor.get()});
    public static RegistryObject<BlockEntityType<ExtremeCraftingTile>> extreme_crafting_tile = blockEntity("extreme_crafting_tile", ExtremeCraftingTile::new, () -> new Block[]{ModBlocks.extreme_crafting_table.get()});
    public static RegistryObject<BlockEntityType<CompressedChestTile>> compressed_chest_tile = blockEntity("compressed_chest_tile", CompressedChestTile::new, () -> new Block[]{ModBlocks.compressed_chest.get()});



}
