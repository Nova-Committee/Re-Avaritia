package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.common.item.misc.NeutronSpacePreview;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/** One chunk of a lazily requested Neutron Ring structure preview. */
public class S2CNeutronRingPreviewPack {
    private final String id;
    private final int chunkIndex;
    private final int chunkCount;
    private final boolean available;
    private final NeutronSpacePreview preview;

    public S2CNeutronRingPreviewPack(String id, int chunkIndex, int chunkCount, boolean available,
                                     NeutronSpacePreview preview) {
        this.id = id;
        this.chunkIndex = chunkIndex;
        this.chunkCount = chunkCount;
        this.available = available;
        this.preview = preview;
    }

    public S2CNeutronRingPreviewPack(FriendlyByteBuf buf) {
        this.id = buf.readUtf();
        this.chunkIndex = buf.readVarInt();
        this.chunkCount = buf.readVarInt();
        this.available = buf.readBoolean();
        this.preview = NeutronSpacePreview.read(buf);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeVarInt(chunkIndex);
        buf.writeVarInt(chunkCount);
        buf.writeBoolean(available);
        preview.write(buf);
    }

    public static S2CNeutronRingPreviewPack missing(String id) {
        return new S2CNeutronRingPreviewPack(id, 0, 0, false, NeutronSpacePreview.EMPTY);
    }

    public static void send(ServerPlayer player, String id, CompoundTag template) {
        HolderLookup.Provider registries = player.server.registryAccess();
        NeutronSpacePreview preview = NeutronSpacePreview.fromTemplate(template, registries);
        int total = Math.min(preview.positions().length, preview.states().length);
        int chunks = Math.max(1, (total + NeutronSpacePreview.CELLS_PER_CHUNK - 1) / NeutronSpacePreview.CELLS_PER_CHUNK);
        if (total == 0) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new S2CNeutronRingPreviewPack(id, 0, 1, true, preview));
            return;
        }
        for (int i = 0; i < chunks; i++) {
            int from = i * NeutronSpacePreview.CELLS_PER_CHUNK;
            int to = Math.min(total, from + NeutronSpacePreview.CELLS_PER_CHUNK);
            NeutronSpacePreview slice = preview.slice(from, to);
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new S2CNeutronRingPreviewPack(id, i, chunks, true, slice));
        }
    }

    public String id() {
        return id;
    }

    public int chunkIndex() {
        return chunkIndex;
    }

    public int chunkCount() {
        return chunkCount;
    }

    public boolean available() {
        return available;
    }

    public NeutronSpacePreview preview() {
        return preview;
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientPacketProxy.neutronRingPreview.accept(this));
        ctx.get().setPacketHandled(true);
    }
}
