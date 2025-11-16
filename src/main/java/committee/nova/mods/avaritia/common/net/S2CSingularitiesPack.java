package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * S2CSingularitiesPacket
 * Description:
 * @author cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public class S2CSingularitiesPack {

    private final Collection<Singularity> cacheSingularities;

    public S2CSingularitiesPack(Collection<Singularity> cacheSingularities) {
        this.cacheSingularities = cacheSingularities;
    }

    public S2CSingularitiesPack(FriendlyByteBuf buf) {
        List<Singularity> cacheSingularities = new ArrayList<>();

        int cacheSize = buf.readVarInt();

        for (int i = 0; i < cacheSize; i++) {
            var singularity = Singularity.read(buf);

            cacheSingularities.add(singularity);
        }

        this.cacheSingularities = cacheSingularities;
    }

    public void write(FriendlyByteBuf buffer) {
        writeSingularities(buffer, this.cacheSingularities);
    }

    private void writeSingularities(FriendlyByteBuf buffer, Collection<Singularity> singularities) {
        buffer.writeVarInt(singularities.size());
        singularities.forEach(singularity -> {
            buffer.writeResourceLocation(singularity.getRegistryName());
            buffer.writeUtf(singularity.getDisplayName());
            buffer.writeVarIntArray(singularity.getColors());
            buffer.writeBoolean(singularity.getTag() != null);
            buffer.writeVarInt(singularity.getTimeCost());

            if (singularity.getTag() != null) {
                buffer.writeUtf(singularity.getTag());
            } else {
                singularity.getIngredient().toNetwork(buffer);
            }

            buffer.writeVarInt(singularity.getCount());
            buffer.writeBoolean(singularity.isEnabled());
            buffer.writeBoolean(singularity.isRecipeDisabled());
        });
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                SingularityDataManager.getInstance().getCachedSingularities().clear();
                SingularityDataManager.getInstance().getCachedSingularities().putAll(
                        this.cacheSingularities.stream()
                                .collect(Collectors.toMap(Singularity::getRegistryName, s -> s))
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
