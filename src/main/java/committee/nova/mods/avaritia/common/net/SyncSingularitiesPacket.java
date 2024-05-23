package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.common.net.SimplePacketBase;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
@Getter
public class SyncSingularitiesPacket extends SimplePacketBase {

    private List<Singularity> singularities;

    public SyncSingularitiesPacket(){}
    public SyncSingularitiesPacket(List<Singularity> singularities) {
        this.singularities = singularities;
    }
    public SyncSingularitiesPacket(FriendlyByteBuf buffer) {
        this.singularities = SingularityRegistryHandler.getInstance().readFromBuffer(buffer);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        SingularityRegistryHandler.getInstance().writeToBuffer(buffer);
    }

    @Override
    public boolean handle(Context context) {
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            return false;
        context.enqueueWork(() ->
                SingularityRegistryHandler.getInstance().loadSingularities(this)
        );
        return true;
    }
}
