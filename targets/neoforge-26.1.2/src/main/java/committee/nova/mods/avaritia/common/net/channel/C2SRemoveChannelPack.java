package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record C2SRemoveChannelPack(int containerId, byte channelType, int channelId) implements CustomPacketPayload {
    public static final Type<C2SRemoveChannelPack> TYPE = new Type<>(Const.rl("c2s_remove_tesseract_channel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRemoveChannelPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SRemoveChannelPack::containerId,
            ByteBufCodecs.BYTE, C2SRemoveChannelPack::channelType,
            ByteBufCodecs.VAR_INT, C2SRemoveChannelPack::channelId,
            C2SRemoveChannelPack::new);

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final class Handler implements IPayloadHandler<C2SRemoveChannelPack> {
        @Override public void handle(@NotNull C2SRemoveChannelPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                TesseractChannelMenu menu = ChannelPayloadGuard.selector(context, packet.containerId());
                if (menu != null && context.player() instanceof ServerPlayer player
                        && packet.channelType() >= 0 && packet.channelType() <= 2
                        && packet.channelId() >= 0 && packet.channelId() <= 9_999) {
                    menu.removeChannel(player, packet.channelType(), packet.channelId());
                }
            });
        }
    }
}
