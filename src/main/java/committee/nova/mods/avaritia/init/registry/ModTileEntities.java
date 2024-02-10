package committee.nova.mods.avaritia.init.registry;

import com.mojang.datafixers.types.Type;
import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.tile.CompressorTile;
import committee.nova.mods.avaritia.common.tile.ExtremeCraftingTile;
import committee.nova.mods.avaritia.common.tile.collector.AbsNeutronCollectorTile;
import committee.nova.mods.avaritia.common.tile.collector.DefaultNeutronCollectorTile;
import committee.nova.mods.avaritia.common.tile.collector.DenseNeutronCollectorTile;
import committee.nova.mods.avaritia.common.tile.collector.DenserNeutronCollectorTile;
import committee.nova.mods.avaritia.common.tile.collector.DensestNeutronCollectorTile;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:48
 * Version: 1.0
 */
public class ModTileEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Static.MOD_ID);

    //public static RegistryObject<BlockEntityType<InfinitatoTile>> infinitato_tile = blockEntity("infinitato_tile", InfinitatoTile::new, () -> new Block[]{ModBlocks.infinitato.get()});

    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        Type<?> type = Util.fetchChoiceType(References.BLOCK_ENTITY, Static.MOD_ID + ":" + name);
        return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(type));
    }

    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {

    }

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<AbsNeutronCollectorTile>> neutron_collector_tile = blockEntity("neutron_collector_tile", DefaultNeutronCollectorTile::new, () -> new Block[]{ModBlocks.neutron_collector.get()});
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<AbsNeutronCollectorTile>> dense_neutron_collector_tile = blockEntity("dense_neutron_collector_tile", DenseNeutronCollectorTile::new, () -> new Block[]{ModBlocks.dense_neutron_collector.get()});
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<AbsNeutronCollectorTile>> denser_neutron_collector_tile = blockEntity("denser_neutron_collector_tile", DenserNeutronCollectorTile::new, () -> new Block[]{ModBlocks.denser_neutron_collector.get()});
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<AbsNeutronCollectorTile>> densest_neutron_collector_tile = blockEntity("densest_neutron_collector_tile", DensestNeutronCollectorTile::new, () -> new Block[]{ModBlocks.densest_neutron_collector.get()});
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<CompressorTile>> compressor_tile = blockEntity("compressor_tile", CompressorTile::new, () -> new Block[]{ModBlocks.neutron_compressor.get()});
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtremeCraftingTile>> extreme_crafting_tile = blockEntity("extreme_crafting_tile", ExtremeCraftingTile::new, () -> new Block[]{ModBlocks.extreme_crafting_table.get()});



}
