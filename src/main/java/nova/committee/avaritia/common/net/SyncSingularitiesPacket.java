package nova.committee.avaritia.common.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import nova.committee.avaritia.api.common.net.IPacket;
import nova.committee.avaritia.common.item.singularity.Singularity;
import nova.committee.avaritia.init.handler.SingularityRegistryHandler;

import java.util.List;
import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public class SyncSingularitiesPacket extends IPacket<SyncSingularitiesPacket> {

    private List<Singularity> singularities;

    public SyncSingularitiesPacket() {
    }

    public SyncSingularitiesPacket(List<Singularity> singularities) {
        this.singularities = singularities;
    }

    public List<Singularity> getSingularities() {
        return this.singularities;
    }


    @Override
    public SyncSingularitiesPacket read(FriendlyByteBuf buf) {
        var singularities = SingularityRegistryHandler.getInstance().readFromBuffer(buf);

        return new SyncSingularitiesPacket(singularities);
    }

    @Override
    public void write(SyncSingularitiesPacket msg, FriendlyByteBuf buf) {
        SingularityRegistryHandler.getInstance().writeToBuffer(buf);
    }

    @Override
    public void run(SyncSingularitiesPacket msg, Supplier<NetworkEvent.Context> ctx) {
    	if(ctx.get().getDirection().getReceptionSide().isServer())
    		return;
    	
        ctx.get().enqueueWork(() -> {
            SingularityRegistryHandler.getInstance().loadSingularities(msg);
        });

        ctx.get().setPacketHandled(true);
    }
}
