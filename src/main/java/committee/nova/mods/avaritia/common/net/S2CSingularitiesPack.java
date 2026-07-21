package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/** Synchronizes the server's already-filtered effective singularity snapshot. */
public class S2CSingularitiesPack {
    private final Collection<Singularity> singularities;

    public S2CSingularitiesPack(Collection<Singularity> singularities) {
        this.singularities = singularities.stream().map(Singularity::copy).toList();
    }

    public S2CSingularitiesPack(FriendlyByteBuf buffer) {
        List<Singularity> decoded = new ArrayList<>();
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            decoded.add(Singularity.read(buffer));
        }
        this.singularities = decoded;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.singularities.size());
        this.singularities.forEach(singularity -> Singularity.write(buffer, singularity));
    }

    public void run(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> SingularityReloadListener.INSTANCE.replaceEffectiveSnapshot(this.singularities)));
        contextSupplier.get().setPacketHandled(true);
    }
}
