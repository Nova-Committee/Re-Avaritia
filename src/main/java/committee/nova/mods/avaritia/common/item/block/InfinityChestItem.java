package committee.nova.mods.avaritia.common.item.block;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/**
 * @author cnlimiter
 */
public class InfinityChestItem extends BlockItem {
    public InfinityChestItem(Block block) {
        super(block, new Properties().rarity(ModRarities.LEGEND.getValue()));
    }
}
