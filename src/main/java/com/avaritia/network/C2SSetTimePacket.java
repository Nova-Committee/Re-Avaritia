package com.avaritia.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record C2SSetTimePacket(int time) implements CustomPacketPayload {
    public static final Type<C2SSetTimePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("avaritia", "set_time"));
    public static final StreamCodec<?, C2SSetTimePacket> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(C2SSetTimePacket::new, C2SSetTimePacket::time);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
