package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.iface.ITileIO;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
public record C2SSideConfigPacket(BlockPos pos, SideConfiguration sideConfig) implements CustomPacketPayload {
    public static final Type<C2SSideConfigPacket> TYPE = new Type<>(Const.rl("c2s_side_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SSideConfigPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            C2SSideConfigPacket::pos,
            SideConfiguration.STREAM_CODEC,
            C2SSideConfigPacket::sideConfig,
            C2SSideConfigPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SSideConfigPacket> {
        @Override
        public void handle(@NotNull C2SSideConfigPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player)  {
                    if (player.level() instanceof ServerLevel level) {
                        BlockEntity tile = level.getBlockEntity(packet.pos);

                        if (tile instanceof ITileIO tileIO) {
                            // 验证玩家是否有权限配置这个方块
                            if (level.getBlockEntity(packet.pos) == null) {
                                return; // 没有权限
                            }

                            // 应用新的配置
                            tileIO.setSideConfiguration(packet.sideConfig);

                            Const.LOGGER.debug("方块配置已更新");

                            // 标记方块实体为已更改，触发保存
                            tileIO.setIOChange();

                        }
                    }
                }

            });
        }
    }



}
