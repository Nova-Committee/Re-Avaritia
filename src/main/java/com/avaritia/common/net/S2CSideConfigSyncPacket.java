package com.avaritia.common.net;

import com.avaritia.Const;
import com.avaritia.api.iface.ITileIO;
import com.avaritia.core.io.SideConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
public record S2CSideConfigSyncPacket(BlockPos pos, SideConfiguration sideConfig) implements CustomPacketPayload {
    public static final Type<S2CSideConfigSyncPacket> TYPE = new Type<>(Const.rl("s2c_side_config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSideConfigSyncPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            S2CSideConfigSyncPacket::pos,
            SideConfiguration.STREAM_CODEC,
            S2CSideConfigSyncPacket::sideConfig,
            S2CSideConfigSyncPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<S2CSideConfigSyncPacket> {
        @Override
        public void handle(@NotNull S2CSideConfigSyncPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                ClientLevel level = Minecraft.getInstance().level;
                if (level == null) return;

                BlockEntity tile = level.getBlockEntity(packet.pos);

                if (tile instanceof ITileIO tileIO) {
                    // 应用同步的配置
                    tileIO.setSideConfiguration(packet.sideConfig);
                }
            });
        }
    }



}
