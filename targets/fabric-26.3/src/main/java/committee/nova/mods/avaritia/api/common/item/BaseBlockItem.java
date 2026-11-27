package committee.nova.mods.avaritia.api.common.item;

import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.world.item.BlockItem;
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
        super(block, ModItems.properties());
    }

    public BaseBlockItem(Block block, Function<Properties, Properties> properties) {
        super(block, properties.apply(ModItems.properties()));
    }
}