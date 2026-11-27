package committee.nova.mods.avaritia.common.net.channel;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum ChannelAction {
    ADD, REMOVE, SET;

    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelAction> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeByte(value.ordinal()),
            buffer -> {
                int id = buffer.readUnsignedByte();
                return id < values().length ? values()[id] : SET;
            });
}
