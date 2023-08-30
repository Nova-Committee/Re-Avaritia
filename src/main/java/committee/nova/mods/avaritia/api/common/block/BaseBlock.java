package committee.nova.mods.avaritia.api.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 15:06
 * Version: 1.0
 */
public class BaseBlock extends Block {
    public BaseBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
    public BaseBlock(Material color, Function<Properties, Properties> properties) {
        super(properties.apply(Properties.of(color)));
    }

    public BaseBlock(Material color, SoundType sound, float hardness, float resistance) {
        super(Properties.of(color).sound(sound).strength(hardness, resistance));
    }


    public BaseBlock(Material color, SoundType sound, float hardness, float resistance, boolean tool) {
        super(
                tool ? Properties.of(color).sound(sound).strength(hardness, resistance).requiresCorrectToolForDrops()
                        : Properties.of(color).sound(sound).strength(hardness, resistance)
        );
    }
}
