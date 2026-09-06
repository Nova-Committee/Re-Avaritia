package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public record InfinityBucketControl(boolean creatureSelected, int selectedIndex) {
    public static final InfinityBucketControl DEFAULT = new InfinityBucketControl(false, 0);
    public static final Codec<InfinityBucketControl> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("creature", false).forGetter(InfinityBucketControl::creatureSelected),
            Codec.INT.optionalFieldOf("index", 0).forGetter(InfinityBucketControl::selectedIndex)
    ).apply(instance, InfinityBucketControl::new));
    public static final StreamCodec<ByteBuf, InfinityBucketControl> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            InfinityBucketControl::creatureSelected,
            ByteBufCodecs.VAR_INT,
            InfinityBucketControl::selectedIndex,
            InfinityBucketControl::new
    );

    public InfinityBucketControl selectFluid(int index) {
        return new InfinityBucketControl(false, Math.max(0, index));
    }

    public InfinityBucketControl selectCreature(int index) {
        return new InfinityBucketControl(true, Math.max(0, index));
    }

    public InfinityBucketControl clamp(int fluidCount, int creatureCount) {
        if (creatureSelected) {
            if (creatureCount <= 0) {
                return new InfinityBucketControl(false, fluidCount <= 0 ? 0 : Mth.clamp(selectedIndex, 0, fluidCount - 1));
            }
            return new InfinityBucketControl(true, Mth.clamp(selectedIndex, 0, creatureCount - 1));
        }
        if (fluidCount <= 0) {
            return new InfinityBucketControl(creatureCount > 0, 0);
        }
        return new InfinityBucketControl(false, Mth.clamp(selectedIndex, 0, fluidCount - 1));
    }
}
