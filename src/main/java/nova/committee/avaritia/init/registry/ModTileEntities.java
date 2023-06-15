package nova.committee.avaritia.init.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import nova.committee.avaritia.common.tile.CompressorTileEntity;
import nova.committee.avaritia.common.tile.ExtremeCraftingTile;
import nova.committee.avaritia.common.tile.InfinitatoTile;
import nova.committee.avaritia.common.tile.NeutronCollectorTile;
import nova.committee.avaritia.util.registry.RegistryUtil;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 9:48
 * Version: 1.0
 */
public class ModTileEntities {

    public static BlockEntityType<ExtremeCraftingTile> extreme_crafting_tile = RegistryUtil.blockEntity("extreme_crafting_tile", ExtremeCraftingTile::new, () -> new Block[]{ModBlocks.extreme_crafting_table}).get();
    public static BlockEntityType<NeutronCollectorTile> neutron_collector_tile = RegistryUtil.blockEntity("neutron_collector_tile", NeutronCollectorTile::new, () -> new Block[]{ModBlocks.neutron_collector}).get();
    public static BlockEntityType<CompressorTileEntity> compressor_tile = RegistryUtil.blockEntity("compressor_tile", CompressorTileEntity::new, () -> new Block[]{ModBlocks.compressor}).get();

    public static BlockEntityType<InfinitatoTile> infinitato_tile = RegistryUtil.blockEntity("infinitato_tile", InfinitatoTile::new, () -> new Block[]{ModBlocks.infinitato}).get();


}
