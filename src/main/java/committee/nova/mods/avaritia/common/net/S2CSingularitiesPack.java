package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * S2CSingularitiesPacket
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public class S2CSingularitiesPack {

    private final List<Singularity> singularities;

    public S2CSingularitiesPack(List<Singularity> singularities) {
        this.singularities = singularities;
    }

    public S2CSingularitiesPack(FriendlyByteBuf buf) {
        this.singularities = SingularityRegistryHandler.getInstance().readFromBuffer(buf);
    }

    public void write(FriendlyByteBuf buf) {
        SingularityRegistryHandler.getInstance().writeToBuffer(buf);
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            SingularityRegistryHandler.getInstance().loadSingularities(this);
        });
        ctx.get().setPacketHandled(true);
    }

    public List<Singularity> getSingularities() {
        return this.singularities;
    }
}
