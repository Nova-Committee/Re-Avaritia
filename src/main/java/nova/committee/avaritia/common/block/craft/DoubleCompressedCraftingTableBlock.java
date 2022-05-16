package nova.committee.avaritia.common.block.craft;

import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 6:51
 * Version: 1.0
 */
public class DoubleCompressedCraftingTableBlock extends CraftingTableBlock {

    public DoubleCompressedCraftingTableBlock() {
        super(BlockBehaviour.Properties.of(Material.WOOD).strength(20F).sound(SoundType.WOOD));
        setRegistryName("double_compressed_crafting_table");
    }

}
