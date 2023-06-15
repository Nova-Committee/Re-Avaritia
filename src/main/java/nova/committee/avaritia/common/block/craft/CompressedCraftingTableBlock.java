package nova.committee.avaritia.common.block.craft;

import net.minecraft.world.level.block.SoundType;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/1 21:44
 * Version: 1.0
 */
public class CompressedCraftingTableBlock extends AbstractCraftingTable {
    public CompressedCraftingTableBlock() {
        super(SoundType.WOOD, 5F, 100F, true, "compressed_crafting_table");
    }

}
