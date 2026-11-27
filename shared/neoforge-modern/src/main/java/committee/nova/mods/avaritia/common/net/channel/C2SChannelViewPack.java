package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record C2SChannelViewPack(int containerId, byte sortType, byte viewType) implements CustomPacketPayload {
    public static final Type<C2SChannelViewPack> TYPE = new Type<>(Const.rl("c2s_tesseract_view"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SChannelViewPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SChannelViewPack::containerId,
            ByteBufCodecs.BYTE, C2SChannelViewPack::sortType,
            ByteBufCodecs.BYTE, C2SChannelViewPack::viewType,
            C2SChannelViewPack::new);

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final class Handler implements IPayloadHandler<C2SChannelViewPack> {
        @Override public void handle(@NotNull C2SChannelViewPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                TesseractMenu menu = ChannelPayloadGuard.storage(context, packet.containerId());
                if (menu != null && packet.sortType() >= 0 && packet.sortType() <= 7
                        && packet.viewType() >= 0 && packet.viewType() <= 2) {
                    menu.setViewFromClient(packet.sortType(), packet.viewType());
                }
            });
        }
    }
}
