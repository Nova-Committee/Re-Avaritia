package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.common.net.IPacket;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public class SyncSingularitiesPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(Static.MOD_ID, "sync_singularities");

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
    public @NotNull ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(@NotNull FriendlyByteBuf pBuffer) {
        SingularityRegistryHandler.getInstance().writeToBuffer(pBuffer);
    }

    public static void run(SyncSingularitiesPacket msg, IPayloadContext ctx) {
        ctx.workHandler().execute(() -> {
            SingularityRegistryHandler.getInstance().loadSingularities(msg);
        });
    }

}
