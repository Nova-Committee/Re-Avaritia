package nova.committee.avaritia.init.compat;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import nova.committee.avaritia.common.block.CompressorBlock;
import nova.committee.avaritia.common.tile.CompressorTileEntity;
import nova.committee.avaritia.init.ModTooltips;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/15 23:21
 * Version: 1.0
 */
@WailaPlugin
public class JadeCompat implements IWailaPlugin {
    @Override
    public void register(IRegistrar registrar) {
        registrar.registerComponentProvider((tooltip, accessor, config) -> {
            var compressor = (CompressorTileEntity) accessor.getBlockEntity();
            var recipe = compressor.getActiveRecipe();

            if (recipe != null) {
                var output = recipe.getResultItem();
                tooltip.add(ModTooltips.CRAFTING.args(output.getCount(), output.getHoverName()).build());
            }
        }, TooltipPosition.BODY, CompressorBlock.class);
    }
}
