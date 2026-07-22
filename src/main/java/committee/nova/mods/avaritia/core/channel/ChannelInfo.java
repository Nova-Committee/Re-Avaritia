package committee.nova.mods.avaritia.core.channel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/** Stable reference to one owner's Tesseract channel. */
public record ChannelInfo(UUID owner, int id) {
    public static final int MAX_ID = 9_999;
    public static final Codec<ChannelInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(ChannelInfo::owner),
            Codec.intRange(0, MAX_ID).fieldOf("id").forGetter(ChannelInfo::id)
    ).apply(instance, ChannelInfo::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelInfo> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ChannelInfo::owner,
            ByteBufCodecs.VAR_INT, ChannelInfo::id,
            ChannelInfo::new
    );

    public ChannelInfo {
        if (owner == null) {
            throw new IllegalArgumentException("Channel owner cannot be null");
        }
        if (id < 0 || id > MAX_ID) {
            throw new IllegalArgumentException("Channel id out of range: " + id);
        }
    }
}
