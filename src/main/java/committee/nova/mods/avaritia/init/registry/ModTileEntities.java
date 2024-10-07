package committee.nova.mods.avaritia.init.registry;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.client.render.tile.CompressedChestRenderer;
import committee.nova.mods.avaritia.common.block.craft.ModCraftingTier;
import committee.nova.mods.avaritia.common.tile.CompressedChestTile;
import committee.nova.mods.avaritia.common.tile.CompressorTile;
import committee.nova.mods.avaritia.common.tile.ModCraftingTile;
import committee.nova.mods.avaritia.common.tile.collector.BaseNeutronCollectorTile;
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
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Static.MOD_ID);

    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(null));
    }

    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {
        BlockEntityRenderers.register(compressed_chest_tile.get(), CompressedChestRenderer::new);
    }

    public static RegistryObject<BlockEntityType<BaseNeutronCollectorTile>> neutron_collector_tile = blockEntity(
            "neutron_collector_tile",
            (BaseNeutronCollectorTile::new),
            () -> new Block[]{
                    ModBlocks.neutron_collector.get(),
                    ModBlocks.dense_neutron_collector.get(),
                    ModBlocks.denser_neutron_collector.get(),
                    ModBlocks.densest_neutron_collector.get()
    }
    );
    public static RegistryObject<BlockEntityType<CompressorTile>> compressor_tile = blockEntity("compressor_tile", CompressorTile::new, () -> new Block[]{ModBlocks.neutron_compressor.get()});
    public static RegistryObject<BlockEntityType<ModCraftingTile>> sculk_crafting_tile = blockEntity("sculk_crafting_tile", (blockPos, blockState) -> new ModCraftingTile(blockPos, blockState, ModCraftingTier.SCULK.size), () -> new Block[]{ModBlocks.sculk_crafting_table.get()});
    public static RegistryObject<BlockEntityType<ModCraftingTile>> nether_crafting_tile = blockEntity("nether_crafting_tile", (blockPos, blockState) -> new ModCraftingTile(blockPos, blockState, ModCraftingTier.NETHER.size), () -> new Block[]{ModBlocks.nether_crafting_table.get()});
    public static RegistryObject<BlockEntityType<ModCraftingTile>> end_crafting_tile = blockEntity("end_crafting_tile", (blockPos, blockState) -> new ModCraftingTile(blockPos, blockState, ModCraftingTier.END.size), () -> new Block[]{ModBlocks.end_crafting_table.get()});
    public static RegistryObject<BlockEntityType<ModCraftingTile>> extreme_crafting_tile = blockEntity("extreme_crafting_tile", (blockPos, blockState) -> new ModCraftingTile(blockPos, blockState, ModCraftingTier.EXTREME.size), () -> new Block[]{ModBlocks.extreme_crafting_table.get()});
    public static RegistryObject<BlockEntityType<CompressedChestTile>> compressed_chest_tile = blockEntity("compressed_chest_tile", CompressedChestTile::new, () -> new Block[]{ModBlocks.compressed_chest.get()});



}
