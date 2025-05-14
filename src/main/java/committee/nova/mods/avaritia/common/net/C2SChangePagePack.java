package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.iface.IChangePage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/**
 * C2SJEIGhostPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public record C2SChangePagePack(int page) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SChangePagePack> TYPE = new CustomPacketPayload.Type<>(Static.rl("c2s_change_page"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SChangePagePack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            C2SChangePagePack::page,
            C2SChangePagePack::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SChangePagePack> {
        @Override
        public void handle(@NotNull C2SChangePagePack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                var player = context.player();
                if (player instanceof ServerPlayer serverPlayer){
                    if (serverPlayer.containerMenu instanceof IChangePage menu) {
                        menu.changePage(packet.page);
                        serverPlayer.containerMenu.broadcastChanges();
                    }
                }
            });
        }
    }
}
