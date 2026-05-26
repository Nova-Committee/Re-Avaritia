package com.avaritia.network;

import com.avaritia.Avaritia;
import com.avaritia.common.tile.NeutronCompressorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/**
 * S2CSingularitiesPacket
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public record C2SCompressorLockPacket(BlockPos pos, boolean lockState) implements CustomPacketPayload {
    public static final Type<C2SCompressorLockPacket> TYPE = new Type<>(Identifier.of(Avaritia.MOD_ID, "c2s_compressor_lock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SCompressorLockPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            C2SCompressorLockPacket::pos,
            ByteBufCodecs.BOOL,
            C2SCompressorLockPacket::lockState,
            C2SCompressorLockPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SCompressorLockPacket> {
        @Override
        public void handle(@NotNull C2SCompressorLockPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player)  {
                    if (player.level() instanceof ServerLevel level) {
                        BlockEntity tile = level.getBlockEntity(packet.pos);

                        if (tile instanceof NeutronCompressorTile compressor) {
                            // 如果是锁定操作且当前有有效配方，则锁定
                            if (packet.lockState && compressor.hasRecipe() && compressor.hasMaterialStack()) {
                                compressor.setRecipeLock(true, compressor.getActiveRecipe());
                                player.sendSystemMessage(Component.translatable("tooltip.avaritia.compressor_lock.message_1"));
                            }
                            // 如果是解锁操作，则解锁
                            else if (!packet.lockState) {
                                compressor.setRecipeLock(false, null);
                                player.sendSystemMessage(Component.translatable("tooltip.avaritia.compressor_lock.message_2"));
                            } else if (!compressor.hasMaterialStack()){
                                // 如果没有有效配方，则发送错误消息
                                player.sendSystemMessage(Component.translatable("tooltip.avaritia.compressor_lock.message_3"));
                            }
                        }
                    }
                }

            });
        }
    }



}
