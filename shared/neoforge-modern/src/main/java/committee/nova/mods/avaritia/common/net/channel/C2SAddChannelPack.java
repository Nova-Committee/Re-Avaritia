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

public record C2SAddChannelPack(int containerId, String name, boolean shared) implements CustomPacketPayload {
    public static final Type<C2SAddChannelPack> TYPE = new Type<>(Const.rl("c2s_add_tesseract_channel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SAddChannelPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SAddChannelPack::containerId,
            ChannelPayloadCodecs.NAME, C2SAddChannelPack::name,
            ByteBufCodecs.BOOL, C2SAddChannelPack::shared,
            C2SAddChannelPack::new);

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final class Handler implements IPayloadHandler<C2SAddChannelPack> {
        @Override public void handle(@NotNull C2SAddChannelPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                TesseractChannelMenu menu = ChannelPayloadGuard.selector(context, packet.containerId());
                if (menu != null && context.player() instanceof ServerPlayer player) {
                    menu.addChannel(player, packet.name(), packet.shared());
                }
            });
        }
    }
}
