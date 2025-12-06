package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.init.data.listener.SingularityReloadListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * S2CSingularitiesPacket
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public record S2CSingularitiesPack(Collection<Singularity> singularities) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSingularitiesPack> TYPE = new CustomPacketPayload.Type<>(Const.rl("s2c_singularities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSingularitiesPack> STREAM_CODEC = StreamCodec.composite(
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
            context.enqueueWork(() -> {
                SingularityReloadListener.INSTANCE.getDataSingularities().clear();
                SingularityReloadListener.INSTANCE.setDataSingularities(packet.singularities.stream()
                        .collect(Collectors.toMap(Singularity::getRegistryName, s -> s)));
            });
        }
    }



}
