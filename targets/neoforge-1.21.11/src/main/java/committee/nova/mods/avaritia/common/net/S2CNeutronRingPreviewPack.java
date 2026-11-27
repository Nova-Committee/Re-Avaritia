package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreview;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/** One chunk of a lazily requested Neutron Ring structure preview. */
public record S2CNeutronRingPreviewPack(String id, int chunkIndex, int chunkCount, boolean available,
                                        NeutronSpacePreview preview) implements CustomPacketPayload {
    public static final Type<S2CNeutronRingPreviewPack> TYPE = new Type<>(Const.rl("s2c_neutron_ring_preview"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CNeutronRingPreviewPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, S2CNeutronRingPreviewPack::id,
            ByteBufCodecs.VAR_INT, S2CNeutronRingPreviewPack::chunkIndex,
            ByteBufCodecs.VAR_INT, S2CNeutronRingPreviewPack::chunkCount,
            ByteBufCodecs.BOOL, S2CNeutronRingPreviewPack::available,
            NeutronSpacePreview.STREAM_CODEC, S2CNeutronRingPreviewPack::preview,
            S2CNeutronRingPreviewPack::new);

    public static S2CNeutronRingPreviewPack missing(String id) {
        return new S2CNeutronRingPreviewPack(id, 0, 0, false, NeutronSpacePreview.EMPTY);
    }

    public static void send(ServerPlayer player, String id, CompoundTag template) {
        NeutronSpacePreview preview = NeutronSpacePreview.fromTemplate(template, player.registryAccess());
        int total = Math.min(preview.positions().length, preview.states().length);
        int chunks = Math.max(1, (total + NeutronSpacePreview.CELLS_PER_CHUNK - 1) / NeutronSpacePreview.CELLS_PER_CHUNK);
        if (total == 0) {
            PacketDistributor.sendToPlayer(player, new S2CNeutronRingPreviewPack(id, 0, 1, true, preview));
            return;
        }
        for (int i = 0; i < chunks; i++) {
            int from = i * NeutronSpacePreview.CELLS_PER_CHUNK;
            int to = Math.min(total, from + NeutronSpacePreview.CELLS_PER_CHUNK);
            NeutronSpacePreview slice = preview.slice(from, to);
            PacketDistributor.sendToPlayer(player, new S2CNeutronRingPreviewPack(id, i, chunks, true, slice));
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<S2CNeutronRingPreviewPack> {
        @Override
        public void handle(@NotNull S2CNeutronRingPreviewPack packet, IPayloadContext context) {
            context.enqueueWork(() -> ClientPacketProxy.neutronRingPreview.accept(packet));
        }
    }
}
