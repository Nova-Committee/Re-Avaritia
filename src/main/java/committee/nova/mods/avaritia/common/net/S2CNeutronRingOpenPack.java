package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.common.item.misc.NeutronRingContents;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/** Opens the Neutron Ring library with metadata only. Full previews are fetched lazily. */
public class S2CNeutronRingOpenPack {
    private final List<Entry> spaces;
    private final Optional<String> selectedId;
    private final int hand;
    private final UUID storageId;
    private final NeutronRingContents.Size size;

    public record Entry(String id, String name, int sizeX, int sizeY, int sizeZ, int blocks) {
        public void write(FriendlyByteBuf buf) {
            buf.writeUtf(id);
            buf.writeUtf(name);
            buf.writeVarInt(sizeX);
            buf.writeVarInt(sizeY);
            buf.writeVarInt(sizeZ);
            buf.writeVarInt(blocks);
        }

        public static Entry read(FriendlyByteBuf buf) {
            return new Entry(buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt());
        }
    }

    public S2CNeutronRingOpenPack(List<Entry> spaces, Optional<String> selectedId, int hand, UUID storageId,
                                  NeutronRingContents.Size size) {
        this.spaces = List.copyOf(spaces);
        this.selectedId = selectedId;
        this.hand = hand;
        this.storageId = storageId;
        this.size = size;
    }

    public S2CNeutronRingOpenPack(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(Entry.read(buf));
        }
        this.spaces = List.copyOf(entries);
        this.selectedId = buf.readBoolean() ? Optional.of(buf.readUtf()) : Optional.empty();
        this.hand = buf.readVarInt();
        this.storageId = buf.readUUID();
        this.size = NeutronRingContents.Size.read(buf);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(spaces.size());
        for (Entry entry : spaces) {
            entry.write(buf);
        }
        buf.writeBoolean(selectedId.isPresent());
        selectedId.ifPresent(buf::writeUtf);
        buf.writeVarInt(hand);
        buf.writeUUID(storageId);
        size.write(buf);
    }

    public static S2CNeutronRingOpenPack from(MinecraftServer server, NeutronRingContents data, InteractionHand hand) {
        List<Entry> spaces = NeutronRingSavedData.get(server).list(data.storageId()).stream()
                .map(info -> new Entry(info.id(), info.name(), info.sizeX(), info.sizeY(), info.sizeZ(), info.blocks()))
                .toList();
        return new S2CNeutronRingOpenPack(spaces, data.selectedId(), hand.ordinal(), data.storageId(), data.size());
    }

    public List<Entry> spaces() {
        return spaces;
    }

    public Optional<String> selectedId() {
        return selectedId;
    }

    public int hand() {
        return hand;
    }

    public UUID storageId() {
        return storageId;
    }

    public NeutronRingContents.Size size() {
        return size;
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientPacketProxy.neutronRingOpen.accept(this));
        ctx.get().setPacketHandled(true);
    }
}
