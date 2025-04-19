package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * S2CSingularitiesPacket
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public class S2CSingularitiesPack implements CustomPacketPayload {
    public static final ResourceLocation ID = Static.rl( "sync_singularities");
    public static final CustomPacketPayload.Type<S2CSingularitiesPack> TYPE = new CustomPacketPayload.Type<>(Static.rl("config_value"));

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

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
