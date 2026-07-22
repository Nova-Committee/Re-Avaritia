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

public record C2SRenameChannelPack(int containerId, byte channelType, int channelId, String name)
        implements CustomPacketPayload {
    public static final Type<C2SRenameChannelPack> TYPE = new Type<>(Const.rl("c2s_rename_tesseract_channel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRenameChannelPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SRenameChannelPack::containerId,
            ByteBufCodecs.BYTE, C2SRenameChannelPack::channelType,
            ByteBufCodecs.VAR_INT, C2SRenameChannelPack::channelId,
            ChannelPayloadCodecs.NAME, C2SRenameChannelPack::name,
            C2SRenameChannelPack::new);

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final class Handler implements IPayloadHandler<C2SRenameChannelPack> {
        @Override public void handle(@NotNull C2SRenameChannelPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                TesseractChannelMenu menu = ChannelPayloadGuard.selector(context, packet.containerId());
                if (menu != null && context.player() instanceof ServerPlayer player
                        && packet.channelType() >= 0 && packet.channelType() <= 2
                        && packet.channelId() >= 0 && packet.channelId() <= 9_999) {
                    menu.renameChannel(player, packet.channelType(), packet.channelId(), packet.name());
                }
            });
        }
    }
}
