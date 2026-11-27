package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.channel.Channel;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record S2CChannelActionPack(ChannelAction action, byte channelType, int channelId, String name)
        implements CustomPacketPayload {
    public static final Type<S2CChannelActionPack> TYPE = new Type<>(Const.rl("s2c_tesseract_channel_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CChannelActionPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(id -> id >= 0 && id < ChannelAction.values().length ? ChannelAction.values()[id] : ChannelAction.SET,
                    ChannelAction::ordinal), S2CChannelActionPack::action,
            ByteBufCodecs.BYTE, S2CChannelActionPack::channelType,
            ByteBufCodecs.VAR_INT, S2CChannelActionPack::channelId,
            ChannelPayloadCodecs.NAME, S2CChannelActionPack::name,
            S2CChannelActionPack::new);

    public static S2CChannelActionPack clearSelection() {
        return new S2CChannelActionPack(ChannelAction.SET, (byte) -1, -1, Channel.DEFAULT_NAME);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<S2CChannelActionPack> {
        @Override
        public void handle(@NotNull S2CChannelActionPack packet, IPayloadContext context) {
            context.enqueueWork(() -> ClientChannelManager.getInstance()
                    .applyAction(packet.action(), packet.channelType(), packet.channelId(), packet.name()));
        }
    }
}
