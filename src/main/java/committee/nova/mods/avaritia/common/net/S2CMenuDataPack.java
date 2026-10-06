package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.common.net.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Supplier;

/** Extra menu construction data, sent immediately before vanilla open-screen synchronization. */
public final class S2CMenuDataPack {
    public final int containerId;
    public final byte[] data;
    public S2CMenuDataPack(int containerId, byte[] data) { this.containerId = containerId; this.data = data; }
    public S2CMenuDataPack(FriendlyByteBuf buffer) {
        containerId = buffer.readVarInt();
        if (containerId < 1 || containerId > 100) throw new IllegalArgumentException("Invalid menu id");
        data = buffer.readByteArray(1048500);
    }
    public void write(FriendlyByteBuf buffer) { buffer.writeVarInt(containerId); buffer.writeByteArray(data); }
    public void run(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> ClientPacketProxy.handle(this));
        context.get().setPacketHandled(true);
    }
}
