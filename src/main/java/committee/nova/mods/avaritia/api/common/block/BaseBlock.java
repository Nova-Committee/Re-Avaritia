package committee.nova.mods.avaritia.api.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 15:06
 * Version: 1.0
 */
public  class BaseBlock extends Block {
    public BaseBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
    public BaseBlock(Function<Properties, Properties> properties) {
        super(properties.apply(Properties.of()));
    }

    public BaseBlock(MapColor color, SoundType sound, float hardness, float resistance) {
        super(Properties.of().sound(sound).strength(hardness, resistance).mapColor(color));
    }

    public BaseBlock(SoundType sound, float hardness, float resistance) {
        super(Properties.of().sound(sound).strength(hardness, resistance));
    }

    public BaseBlock(SoundType sound, float hardness, float resistance, boolean tool) {
        super(
                tool ? Properties.of().sound(sound).strength(hardness, resistance).requiresCorrectToolForDrops()
                        : Properties.of().sound(sound).strength(hardness, resistance)
        );
    }

    public BaseBlock(MapColor color, SoundType sound, float hardness, float resistance, boolean tool) {
        super(
                tool ? Properties.of().sound(sound).strength(hardness, resistance).mapColor(color).requiresCorrectToolForDrops()
                        : Properties.of().sound(sound).strength(hardness, resistance).mapColor(color)
        );
    }
}
