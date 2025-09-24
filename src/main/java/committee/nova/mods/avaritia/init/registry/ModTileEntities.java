package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.render.tile.InfinityChestRender;
import committee.nova.mods.avaritia.client.render.tile.TesseractRender;
import committee.nova.mods.avaritia.client.render.tile.CompressedChestRender;
import committee.nova.mods.avaritia.common.tile.*;
import committee.nova.mods.avaritia.common.tile.InfinityChestTile;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Const.MOD_ID);

    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(null));
    }

    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {
        BlockEntityRenderers.register(compressed_chest_tile.get(), CompressedChestRender::new);
        BlockEntityRenderers.register(tesseract_tile.get(), TesseractRender::new);
        BlockEntityRenderers.register(infinity_chest_tile.get(), InfinityChestRender::new);
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
    public static RegistryObject<BlockEntityType<TesseractTile>> tesseract_tile = blockEntity("tesseract_tile", TesseractTile::new, () -> new Block[]{ModBlocks.tesseract.get()});
    public static RegistryObject<BlockEntityType<InfinityChestTile>> infinity_chest_tile = blockEntity("infinity_chest_tile", InfinityChestTile::new, () -> new Block[]{ModBlocks.infinity_chest.get()});
}
