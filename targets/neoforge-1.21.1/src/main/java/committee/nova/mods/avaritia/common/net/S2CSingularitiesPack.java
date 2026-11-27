package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/** Synchronizes the server's already-filtered effective singularity snapshot. */
public record S2CSingularitiesPack(Collection<Singularity> singularities) implements CustomPacketPayload {
    public S2CSingularitiesPack {
        singularities = singularities.stream().map(Singularity::copy).toList();
    }

    public static final CustomPacketPayload.Type<S2CSingularitiesPack> TYPE =
            new CustomPacketPayload.Type<>(Const.rl("s2c_singularities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSingularitiesPack> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, Singularity.STREAM_CODEC),
                    S2CSingularitiesPack::singularities,
                    S2CSingularitiesPack::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<S2CSingularitiesPack> {
        @Override
        public void handle(@NotNull S2CSingularitiesPack packet, IPayloadContext context) {
            context.enqueueWork(() -> SingularityReloadListener.INSTANCE
                    .replaceEffectiveSnapshot(packet.singularities));
        }
    }
}
