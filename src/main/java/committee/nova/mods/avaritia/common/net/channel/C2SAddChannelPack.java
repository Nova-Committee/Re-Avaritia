package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import committee.nova.mods.avaritia.core.channel.ServerChannelManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record C2SAddChannelPack(int containerId, String name, boolean pub) implements CustomPacketPayload {
    public static final Type<C2SAddChannelPack> TYPE = new Type<>(Const.rl("c2s_add_tesseract_channel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SAddChannelPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SAddChannelPack::containerId,
            ChannelPayloadCodecs.NAME, C2SAddChannelPack::name,
            ByteBufCodecs.BOOL, C2SAddChannelPack::pub,
            C2SAddChannelPack::new);

    public C2SAddChannelPack { name = ChannelPayloadCodecs.normalizeName(name); }
    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final class Handler implements IPayloadHandler<C2SAddChannelPack> {
        @Override public void handle(@NotNull C2SAddChannelPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                TesseractChannelMenu menu = ChannelPayloadGuard.selectorMenu(context, packet.containerId);
                ServerChannelManager manager = ServerChannelManager.getInstance();
                if (menu != null && manager != null && context.player() instanceof ServerPlayer player) {
                    manager.tryAddChannel(player, packet.name, packet.pub);
                }
            });
        }
    }
}
