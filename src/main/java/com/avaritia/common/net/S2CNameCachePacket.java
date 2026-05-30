//package com.avaritia.common.net;
//
//import com.avaritia.Const;
//import com.avaritia.core.chest.ClientChestManager;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.RegistryFriendlyByteBuf;
//import net.minecraft.network.codec.ByteBufCodecs;
//import net.minecraft.network.codec.StreamCodec;
//import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//import net.neoforged.neoforge.network.handling.IPayloadContext;
//import net.neoforged.neoforge.network.handling.IPayloadHandler;
//import org.jetbrains.annotations.NotNull;
//
///**
// * C2SRenamePack
// *
// * @author cnlimiter
// * @version 1.0
// * @description
// * @date 2024/3/28 14:02
// */
//public record S2CNameCachePacket(CompoundTag name) implements CustomPacketPayload {
//    public static final Type<S2CNameCachePacket> TYPE = new Type<>(Const.rl("s2c_name_cache"));
//    public static final StreamCodec<RegistryFriendlyByteBuf, S2CNameCachePacket> STREAM_CODEC = StreamCodec.composite(
//            ByteBufCodecs.COMPOUND_TAG,
//            S2CNameCachePacket::name,
//            S2CNameCachePacket::new
//    );
//
//    @Override
//    public @NotNull Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
//
//    public static class Handler implements IPayloadHandler<S2CNameCachePacket> {
//        @Override
//        public void handle(@NotNull S2CNameCachePacket packet, IPayloadContext context) {
//            context.enqueueWork(() -> {
//                ClientChestManager.getInstance().setUserCache(packet.name);
//            });
//        }
//    }
//}
