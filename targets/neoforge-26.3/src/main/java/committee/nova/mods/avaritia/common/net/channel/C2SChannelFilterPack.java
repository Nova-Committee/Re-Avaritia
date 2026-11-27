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

public record C2SChannelFilterPack(int containerId, String filter) implements CustomPacketPayload {
    public static final Type<C2SChannelFilterPack> TYPE = new Type<>(Const.rl("c2s_tesseract_filter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SChannelFilterPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SChannelFilterPack::containerId,
            ChannelPayloadCodecs.FILTER, C2SChannelFilterPack::filter,
            C2SChannelFilterPack::new);

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final class Handler implements IPayloadHandler<C2SChannelFilterPack> {
        @Override public void handle(@NotNull C2SChannelFilterPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                TesseractMenu menu = ChannelPayloadGuard.storage(context, packet.containerId());
                if (menu != null) menu.setFilterFromClient(packet.filter());
            });
        }
    }
}
