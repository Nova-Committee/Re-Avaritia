package nova.committee.avaritia.util.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

/**
 * Author cnlimiter
 * CreateTime 2023/6/14 18:19
 * Name BaseItemBlock
 * Description
 */

public class BaseBlockItem extends BlockItem {
    public BaseBlockItem(Block block) {
        super(block, new Item.Properties());
    }

    public BaseBlockItem(Block block, Function<Properties, Properties> properties) {
        super(block, properties.apply(new Properties()));
    }
}