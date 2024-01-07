package committee.nova.mods.avaritia.common.tile.collector;

import committee.nova.mods.avaritia.init.registry.ModItems;
import committee.nova.mods.avaritia.init.registry.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @Project: Avaritia-forge
 * @Author: cnlimiter
 * @CreateTime: 2024/1/7 20:52
 * @Description:
 */
public class DenseNeutronCollectorTile extends AbsNeutronCollectorTile {

    public DenseNeutronCollectorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.dense_neutron_collector_tile.get(), pos, state, ModItems.neutron_nugget.get(), 3600, "dense_neutron_collector");
    }
}
