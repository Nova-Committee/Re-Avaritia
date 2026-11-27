package committee.nova.mods.avaritia.api.init.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/** Burn duration consumed by the native furnace fuel hook. */
public final class FuelBlockItem extends BlockItem {
    private final int burnTime;
    public FuelBlockItem(Block block, Properties properties, int burnTime) {
        super(block, properties);
        this.burnTime = burnTime;
    }
    public int burnTime() { return burnTime; }
}
