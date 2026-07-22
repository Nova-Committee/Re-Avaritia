package committee.nova.mods.avaritia.common.net.channel;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

final class ChannelPayloadCodecs {
    static final StreamCodec<RegistryFriendlyByteBuf, String> NAME = StreamCodec.of(
            (buffer, value) -> buffer.writeUtf(normalizeName(value), 64), buffer -> buffer.readUtf(64));
    static final StreamCodec<RegistryFriendlyByteBuf, String> FILTER = StreamCodec.of(
            (buffer, value) -> buffer.writeUtf(limit(value, 64), 64), buffer -> buffer.readUtf(64));
    static final StreamCodec<RegistryFriendlyByteBuf, String> KIND = StreamCodec.of(
            (buffer, value) -> buffer.writeUtf(limit(value, 16), 16), buffer -> buffer.readUtf(16));
    static final StreamCodec<RegistryFriendlyByteBuf, String> IDENTIFIER = StreamCodec.of(
            (buffer, value) -> buffer.writeUtf(limit(value, 256), 256), buffer -> buffer.readUtf(256));
    static final StreamCodec<RegistryFriendlyByteBuf, Byte> BYTE = StreamCodec.of(
            (buffer, value) -> buffer.writeByte(value), RegistryFriendlyByteBuf::readByte);

    private ChannelPayloadCodecs() { }

    static String normalizeName(String value) {
        String result = limit(value == null ? "" : value.strip(), 64);
        return result.isEmpty() ? "Channel" : result;
    }

    static String limit(String value, int max) {
        String safe = value == null ? "" : value;
        return safe.substring(0, Math.min(max, safe.length()));
    }
}
