package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record C2SSetChannelPack(int containerId, byte channelType, int channelId) implements CustomPacketPayload {
    public static final Type<C2SSetChannelPack> TYPE = new Type<>(Const.rl("c2s_set_tesseract_channel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SSetChannelPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SSetChannelPack::containerId,
            ChannelPayloadCodecs.BYTE, C2SSetChannelPack::channelType,
            ByteBufCodecs.INT, C2SSetChannelPack::channelId,
            C2SSetChannelPack::new);
    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final class Handler implements IPayloadHandler<C2SSetChannelPack> {
        @Override public void handle(@NotNull C2SSetChannelPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                TesseractChannelMenu menu = ChannelPayloadGuard.selectorMenu(context, packet.containerId);
                if (menu != null && packet.channelType >= 0 && packet.channelType <= 2
                        && packet.channelId >= 0 && packet.channelId < 10_000) {
                    menu.setChannel(packet.channelType, packet.channelId);
                }
            });
        }
    }
}
