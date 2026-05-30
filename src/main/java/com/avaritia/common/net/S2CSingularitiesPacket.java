package com.avaritia.common.net;

import com.avaritia.Const;
import com.avaritia.core.singularity.Singularity;
import com.avaritia.core.singularity.SingularityReloadListener;
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
public record S2CSingularitiesPacket(Collection<Singularity> dataSingularities, Collection<Singularity> runSingularities) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSingularitiesPacket> TYPE = new CustomPacketPayload.Type<>(Const.rl("s2c_singularities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSingularitiesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, Singularity.STREAM_CODEC),
            S2CSingularitiesPacket::dataSingularities,
            ByteBufCodecs.collection(ArrayList::new, Singularity.STREAM_CODEC),
            S2CSingularitiesPacket::runSingularities,
            S2CSingularitiesPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<S2CSingularitiesPacket> {
        @Override
        public void handle(@NotNull S2CSingularitiesPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                SingularityReloadListener.INSTANCE.getDataSingularities().clear();
                SingularityReloadListener.INSTANCE.getRunSingularities().clear();
                SingularityReloadListener.INSTANCE.setDataSingularities(packet.dataSingularities.stream()
                        .collect(Collectors.toMap(Singularity::getRegistryName, s -> s))
                );
                SingularityReloadListener.INSTANCE.setRunSingularities(packet.runSingularities.stream()
                        .collect(Collectors.toMap(Singularity::getRegistryName, s -> s))
                );
            });
        }
    }



}
