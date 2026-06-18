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

/**
 * S2CSingularitiesPacket
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public class S2CSingularitiesPack {

    private final Collection<Singularity> dataSingularities;
    private final Collection<Singularity> runSingularities;

    public S2CSingularitiesPack(Collection<Singularity> dataSingularities, Collection<Singularity> runSingularities) {
        this.dataSingularities = dataSingularities;
        this.runSingularities = runSingularities;
    }

    public S2CSingularitiesPack(FriendlyByteBuf buf) {
        List<Singularity> dataSingularities = new ArrayList<>();
        List<Singularity> runSingularities = new ArrayList<>();

        int cacheSize = buf.readVarInt();

        for (int i = 0; i < cacheSize; i++) {
            var singularity = Singularity.read(buf);

            dataSingularities.add(singularity);
        }

        int runSize = buf.readVarInt();

        for (int i = 0; i < runSize; i++) {
            var singularity = Singularity.read(buf);

            runSingularities.add(singularity);
        }

        this.dataSingularities = dataSingularities;
        this.runSingularities = runSingularities;
    }

    public void write(FriendlyByteBuf buffer) {
        writeSingularities(buffer, this.dataSingularities);
        writeSingularities(buffer, this.runSingularities);
    }

    private void writeSingularities(FriendlyByteBuf buffer, Collection<Singularity> singularities) {
        buffer.writeVarInt(singularities.size());
        singularities.forEach(singularity -> Singularity.write(buffer, singularity));
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                SingularityReloadListener.INSTANCE.replaceSingularities(this.dataSingularities, this.runSingularities);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
