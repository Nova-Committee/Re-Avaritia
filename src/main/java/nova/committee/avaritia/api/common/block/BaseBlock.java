package nova.committee.avaritia.api.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

import java.util.function.Function;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 15:06
 * Version: 1.0
 */
public class BaseBlock extends Block {
    public BaseBlock(Material material, Function<Properties, Properties> properties) {
        super((Properties) properties.apply(Properties.of(material)));
    }

    public BaseBlock(Material material, SoundType sound, float hardness, float resistance) {
        super(Properties.of(material).sound(sound).strength(hardness, resistance));
    }

    public BaseBlock(Material material, SoundType sound, float hardness, float resistance, boolean tool) {
        super(Properties.of(material).sound(sound).strength(hardness, resistance).requiresCorrectToolForDrops());
    }
}
