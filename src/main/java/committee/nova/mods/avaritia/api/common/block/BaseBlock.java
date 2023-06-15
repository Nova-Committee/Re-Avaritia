package committee.nova.mods.avaritia.api.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 15:06
 * Version: 1.0
 */
public class BaseBlock extends Block {
    public BaseBlock(MapColor color, Function<Properties, Properties> properties) {
        super(properties.apply(Properties.of().mapColor(color)));
    }

    public BaseBlock(MapColor color, SoundType sound, float hardness, float resistance) {
        super(Properties.of().sound(sound).strength(hardness, resistance).mapColor(color));
    }

    public BaseBlock(MapColor color, SoundType sound, float hardness, float resistance, boolean tool) {
        super(Properties.of().sound(sound).strength(hardness, resistance).mapColor(color).requiresCorrectToolForDrops());
    }
}
