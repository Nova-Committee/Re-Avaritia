package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
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
        List<Singularity> singularities = new ArrayList<>();

        int size = buf.readVarInt();

        for (int i = 0; i < size; i++) {
            var singularity = Singularity.read(buf);

            singularities.add(singularity);
        }

        this.singularities = singularities;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.singularities.size());

        this.singularities.forEach((singularity) -> {
            buffer.writeResourceLocation(singularity.getId());
            buffer.writeUtf(singularity.getName());
            buffer.writeVarIntArray(singularity.getColors());
            buffer.writeBoolean(singularity.getTag() != null);
            buffer.writeVarInt(singularity.getTimeRequired());

            if (singularity.getTag() != null) {
                buffer.writeUtf(singularity.getTag());
            } else {
                singularity.getIngredient().toNetwork(buffer);
            }

            buffer.writeVarInt(singularity.getIngredientCount());
            buffer.writeBoolean(singularity.isEnabled());
            buffer.writeBoolean(singularity.isRecipeDisabled());
        });
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            SingularityDataManager.INSTANCE.getCachedSingularities().clear();
            SingularityDataManager.INSTANCE.getCachedSingularities().addAll(this.singularities);
        });
        ctx.get().setPacketHandled(true);
    }

    public List<Singularity> getSingularities() {
        return this.singularities;
    }
}
