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
public class DenserNeutronCollectorTile extends AbsNeutronCollectorTile {

    public DenserNeutronCollectorTile(BlockPos pos, BlockState state) {
        super(ModTileEntities.denser_neutron_collector_tile.get(), pos, state, ModItems.infinity_ingot.get(), 3600, "denser_neutron_collector");
    }
}
