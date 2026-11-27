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

public record C2SRenameChannelPack(int containerId, String name) implements CustomPacketPayload {
    public static final Type<C2SRenameChannelPack> TYPE = new Type<>(Const.rl("c2s_rename_tesseract_channel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRenameChannelPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SRenameChannelPack::containerId,
            ChannelPayloadCodecs.NAME, C2SRenameChannelPack::name,
            C2SRenameChannelPack::new);
    public C2SRenameChannelPack { name = ChannelPayloadCodecs.normalizeName(name); }
    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final class Handler implements IPayloadHandler<C2SRenameChannelPack> {
        @Override public void handle(@NotNull C2SRenameChannelPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                TesseractChannelMenu menu = ChannelPayloadGuard.selectorMenu(context, packet.containerId);
                if (menu != null) menu.renameChannel(packet.name);
            });
        }
    }
}
