package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.NeutronRingContents;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSavedData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Opens the Neutron Ring library with metadata only. Full previews are fetched lazily. */
public record S2CNeutronRingOpenPack(List<Entry> spaces, Optional<String> selectedId, int hand, UUID storageId,
                                     NeutronRingContents.Size size) implements CustomPacketPayload {
    public record Entry(String id, String name, int sizeX, int sizeY, int sizeZ, int blocks) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Entry::id,
                ByteBufCodecs.STRING_UTF8, Entry::name,
                ByteBufCodecs.VAR_INT, Entry::sizeX,
                ByteBufCodecs.VAR_INT, Entry::sizeY,
                ByteBufCodecs.VAR_INT, Entry::sizeZ,
                ByteBufCodecs.VAR_INT, Entry::blocks,
                Entry::new);
    }

    public static final Type<S2CNeutronRingOpenPack> TYPE = new Type<>(Const.rl("s2c_neutron_ring_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CNeutronRingOpenPack> STREAM_CODEC = StreamCodec.composite(
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), S2CNeutronRingOpenPack::spaces,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), S2CNeutronRingOpenPack::selectedId,
            ByteBufCodecs.VAR_INT, S2CNeutronRingOpenPack::hand,
            UUIDUtil.STREAM_CODEC, S2CNeutronRingOpenPack::storageId,
            NeutronRingContents.Size.STREAM_CODEC, S2CNeutronRingOpenPack::size,
            S2CNeutronRingOpenPack::new);

    public static S2CNeutronRingOpenPack from(MinecraftServer server, NeutronRingContents data, InteractionHand hand) {
        List<Entry> spaces = NeutronRingSavedData.get(server).list(data.storageId()).stream()
                .map(info -> new Entry(info.id(), info.name(), info.sizeX(), info.sizeY(), info.sizeZ(), info.blocks()))
                .toList();
        return new S2CNeutronRingOpenPack(spaces, data.selectedId(), hand.ordinal(), data.storageId(), data.size());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<S2CNeutronRingOpenPack> {
        @Override
        public void handle(@NotNull S2CNeutronRingOpenPack packet, IPayloadContext context) {
            context.enqueueWork(() -> ClientPacketProxy.neutronRingOpen.accept(packet));
        }
    }
}
