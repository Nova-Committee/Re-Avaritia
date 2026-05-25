package com.avaritia.network;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record C2SRenamePack(String name) implements CustomPacketPayload {
    public static final Type<C2SRenamePack> TYPE = new Type<>(Identifier.fromNamespaceAndPath("avaritia", "rename"));
    public static final StreamCodec<?, C2SRenamePack> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(C2SRenamePack::new, C2SRenamePack::name);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
