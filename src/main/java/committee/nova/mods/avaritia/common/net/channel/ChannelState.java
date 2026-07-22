package committee.nova.mods.avaritia.common.net.channel;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum ChannelState {
    COMMON, FULL, NAME;

    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelState> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeByte(value.ordinal()),
            buffer -> {
                int id = buffer.readUnsignedByte();
                return id < values().length ? values()[id] : FULL;
            });
}
