package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record S2CChannelActionPack(ChannelAction action, byte channelType, String name, int channelId)
        implements CustomPacketPayload {
    public static final Type<S2CChannelActionPack> TYPE = new Type<>(Const.rl("s2c_tesseract_channel_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CChannelActionPack> STREAM_CODEC = StreamCodec.composite(
            ChannelAction.STREAM_CODEC, S2CChannelActionPack::action,
            ChannelPayloadCodecs.BYTE, S2CChannelActionPack::channelType,
            ChannelPayloadCodecs.NAME, S2CChannelActionPack::name,
            ByteBufCodecs.INT, S2CChannelActionPack::channelId,
            S2CChannelActionPack::new);
    public S2CChannelActionPack { name = ChannelPayloadCodecs.limit(name, 64); }
    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final class Handler implements IPayloadHandler<S2CChannelActionPack> {
        @Override public void handle(@NotNull S2CChannelActionPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                ClientChannelManager manager = ClientChannelManager.getInstance();
                switch (packet.action) {
                    case ADD -> manager.addChannel(packet.channelType, packet.channelId, packet.name);
                    case REMOVE -> manager.removeChannel(packet.channelType, packet.channelId, packet.name);
                    case SET -> manager.setSelectedChannel(packet.channelType, packet.channelId, packet.name);
                }
            });
        }
    }
}
