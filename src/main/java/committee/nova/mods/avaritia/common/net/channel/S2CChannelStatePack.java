package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.channel.Channel;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record S2CChannelStatePack(ChannelState state, Channel.Data data) implements CustomPacketPayload {
    public static final Type<S2CChannelStatePack> TYPE = new Type<>(Const.rl("s2c_tesseract_channel_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CChannelStatePack> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeByte(packet.state.ordinal());
                ChannelPayloadCodecs.CHANNEL_DATA.encode(buffer, packet.data);
            },
            buffer -> {
                int ordinal = buffer.readUnsignedByte();
                if (ordinal >= ChannelState.values().length) {
                    throw new io.netty.handler.codec.DecoderException("Invalid Tesseract channel state: " + ordinal);
                }
                return new S2CChannelStatePack(ChannelState.values()[ordinal],
                        ChannelPayloadCodecs.CHANNEL_DATA.decode(buffer));
            });

    public static S2CChannelStatePack delta(Channel.Data data) {
        return new S2CChannelStatePack(ChannelState.DELTA, data);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<S2CChannelStatePack> {
        @Override
        public void handle(@NotNull S2CChannelStatePack packet, IPayloadContext context) {
            context.enqueueWork(() -> ClientChannelManager.getInstance().channel().apply(packet.state(), packet.data()));
        }
    }
}
