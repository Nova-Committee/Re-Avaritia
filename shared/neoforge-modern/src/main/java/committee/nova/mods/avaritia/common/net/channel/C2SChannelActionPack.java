package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/** Explicit virtual-slot withdrawal request; the server resolves resource identity by slot. */
public record C2SChannelActionPack(int containerId, int visibleSlot, int amount) implements CustomPacketPayload {
    public static final Type<C2SChannelActionPack> TYPE = new Type<>(Const.rl("c2s_tesseract_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SChannelActionPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SChannelActionPack::containerId,
            ByteBufCodecs.VAR_INT, C2SChannelActionPack::visibleSlot,
            ByteBufCodecs.VAR_INT, C2SChannelActionPack::amount,
            C2SChannelActionPack::new);

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final class Handler implements IPayloadHandler<C2SChannelActionPack> {
        @Override public void handle(@NotNull C2SChannelActionPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                TesseractMenu menu = ChannelPayloadGuard.storage(context, packet.containerId());
                if (menu != null && context.player() instanceof ServerPlayer player
                        && packet.visibleSlot() >= 0 && packet.visibleSlot() < TesseractMenu.CHANNEL_SLOTS
                        && packet.amount() > 0 && packet.amount() <= 64) {
                    menu.withdraw(packet.visibleSlot(), packet.amount(), player);
                }
            });
        }
    }
}
