package committee.nova.mods.avaritia.api.common.block;

import committee.nova.mods.avaritia.init.registry.ModBlocks;
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
public class BaseBlock extends Block {
    public BaseBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public BaseBlock(Function<Properties, Properties> properties) {
        super(properties.apply(ModBlocks.properties()));
    }

    public BaseBlock(MapColor color, SoundType sound, float hardness, float resistance, int lightLevel) {
        super(ModBlocks.properties().sound(sound).strength(hardness, resistance).mapColor(color).lightLevel((e) -> lightLevel));
    }

    public BaseBlock(MapColor color, SoundType sound, float hardness, float resistance) {
        super(ModBlocks.properties().sound(sound).strength(hardness, resistance).mapColor(color));
    }

    public BaseBlock(SoundType sound, float hardness, float resistance) {
        super(ModBlocks.properties().sound(sound).strength(hardness, resistance));
    }

    public BaseBlock(SoundType sound, float hardness, float resistance, boolean tool) {
        super(
                tool ? ModBlocks.properties().sound(sound).strength(hardness, resistance).requiresCorrectToolForDrops()
                        : ModBlocks.properties().sound(sound).strength(hardness, resistance)
        );
    }

    public BaseBlock(MapColor color, SoundType sound, float hardness, float resistance, boolean tool) {
        super(
                tool ? ModBlocks.properties().sound(sound).strength(hardness, resistance).mapColor(color).requiresCorrectToolForDrops()
                        : ModBlocks.properties().sound(sound).strength(hardness, resistance).mapColor(color)
        );
    }

    public BaseBlock(MapColor color, SoundType sound, int hardness, int resistance, boolean tool, Properties properties) {
        super(tool
                ? properties.sound(sound).strength(hardness, resistance).mapColor(color).requiresCorrectToolForDrops()
                : properties.sound(sound).strength(hardness, resistance).mapColor(color));
    }
}
