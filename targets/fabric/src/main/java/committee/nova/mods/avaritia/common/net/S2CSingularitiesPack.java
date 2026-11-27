package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.core.singularity.Singularity;

import net.minecraft.network.FriendlyByteBuf;


import committee.nova.mods.avaritia.api.common.net.PacketContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/** Synchronizes the server's already-filtered effective singularity snapshot. */
public class S2CSingularitiesPack {
    public final Collection<Singularity> singularities;

    public S2CSingularitiesPack(Collection<Singularity> singularities) {
        this.singularities = singularities.stream().map(Singularity::copy).toList();
    }

    public S2CSingularitiesPack(FriendlyByteBuf buffer) {
        List<Singularity> decoded = new ArrayList<>();
        int size = buffer.readVarInt();
        if (size < 0 || size > buffer.readableBytes()) throw new IllegalArgumentException("Invalid singularity count");
        for (int i = 0; i < size; i++) {
            decoded.add(Singularity.read(buffer));
        }
        this.singularities = decoded;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.singularities.size());
        this.singularities.forEach(singularity -> Singularity.write(buffer, singularity));
    }

    public void run(Supplier<PacketContext> contextSupplier) { contextSupplier.get().enqueueWork(() -> committee.nova.mods.avaritia.common.net.ClientPacketProxy.handle(this)); contextSupplier.get().setPacketHandled(true); }
}
