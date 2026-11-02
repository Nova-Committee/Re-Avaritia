package committee.nova.mods.avaritia.api.common.block;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

/**
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 15:06
 * Version: 1.0
 */
public class BaseBlock extends Block {
    /**
     * 26.3 requires {@link BlockBehaviour.Properties} to carry the registry id before the block is constructed
     * (effectiveDrops NPEs otherwise). Registration factories set this key for the duration of block creation;
     * it must only ever be touched on the registering thread.
     */
    private static final ThreadLocal<ResourceKey<Block>> CURRENT_REGISTRATION_KEY = new ThreadLocal<>();

    public static void beginRegistration(ResourceKey<Block> key) {
        CURRENT_REGISTRATION_KEY.set(key);
    }

    public static void endRegistration() {
        CURRENT_REGISTRATION_KEY.remove();
    }

    /** Creates block properties carrying the id of the block currently being registered, if any. */
    public static Properties newProperties() {
        Properties properties = Properties.of();
        ResourceKey<Block> key = CURRENT_REGISTRATION_KEY.get();
        return key == null ? properties : properties.setId(key);
    }

    public BaseBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public BaseBlock(Function<Properties, Properties> properties) {
        super(properties.apply(newProperties()));
    }

    public BaseBlock(MapColor color, SoundType sound, float hardness, float resistance, int lightLevel) {
        super(newProperties().sound(sound).strength(hardness, resistance).mapColor(color).lightLevel((e) -> lightLevel));
    }

    public BaseBlock(MapColor color, SoundType sound, float hardness, float resistance) {
        super(newProperties().sound(sound).strength(hardness, resistance).mapColor(color));
    }

    public BaseBlock(SoundType sound, float hardness, float resistance) {
        super(newProperties().sound(sound).strength(hardness, resistance));
    }

    public BaseBlock(SoundType sound, float hardness, float resistance, boolean tool) {
        super(
                tool ? newProperties().sound(sound).strength(hardness, resistance).requiresCorrectToolForDrops()
                        : newProperties().sound(sound).strength(hardness, resistance)
        );
    }

    public BaseBlock(MapColor color, SoundType sound, float hardness, float resistance, boolean tool) {
        super(
                tool ? newProperties().sound(sound).strength(hardness, resistance).mapColor(color).requiresCorrectToolForDrops()
                        : newProperties().sound(sound).strength(hardness, resistance).mapColor(color)
        );
    }

    public BaseBlock(MapColor metal, SoundType sound, int hardness, int resistance, boolean b, Properties properties) {
        super(properties);
    }
}
