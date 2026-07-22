package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record S2CChannelListPack(Map<Integer, String> mine, Map<Integer, String> terminalOwner,
                                 Map<Integer, String> shared) implements CustomPacketPayload {
    public static final Type<S2CChannelListPack> TYPE = new Type<>(Const.rl("s2c_tesseract_channel_list"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CChannelListPack> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                ChannelPayloadCodecs.encodeMap(buffer, packet.mine());
                ChannelPayloadCodecs.encodeMap(buffer, packet.terminalOwner());
                ChannelPayloadCodecs.encodeMap(buffer, packet.shared());
            },
            buffer -> new S2CChannelListPack(ChannelPayloadCodecs.decodeMap(buffer),
                    ChannelPayloadCodecs.decodeMap(buffer), ChannelPayloadCodecs.decodeMap(buffer)));

    public S2CChannelListPack {
        mine = Map.copyOf(mine);
        terminalOwner = Map.copyOf(terminalOwner);
        shared = Map.copyOf(shared);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<S2CChannelListPack> {
        @Override
        public void handle(@NotNull S2CChannelListPack packet, IPayloadContext context) {
            context.enqueueWork(() -> ClientChannelManager.getInstance()
                    .replaceLists(packet.mine(), packet.terminalOwner(), packet.shared()));
        }
    }
}
