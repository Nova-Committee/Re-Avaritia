package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.common.tile.config.SideConfiguration;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import committee.nova.mods.avaritia.common.tile.NeutronCompressorTile;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 方块配置更新数据包
 * Description: 客户端发送方块配置更新到服务端
 * Author: 幽浮喵
 * Date: 2025/11/01
 * Version: 1.0
 */
public class C2SSideConfigPacket {
    private final BlockPos pos;
    private final SideConfiguration sideConfig;

    public C2SSideConfigPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.sideConfig = SideConfiguration.fromNetwork(buf);
    }

    public C2SSideConfigPacket(BlockPos pos, SideConfiguration sideConfig) {
        this.pos = pos;
        this.sideConfig = sideConfig;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        this.sideConfig.toNetwork(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            Level level = player.level();
            BlockEntity tile = level.getBlockEntity(this.pos);

            if (tile instanceof NeutronCompressorTile compressor) {
                // 验证玩家是否有权限配置这个方块
                if (player.canUseGameMasterBlocks() || level.getBlockEntity(this.pos) == null) {
                    return; // 没有权限
                }

                // 应用新的配置
                compressor.setSideConfiguration(sideConfig);

                // 发送确认消息给玩家
                player.sendSystemMessage(Component.literal("§a[中子压缩器] §f方块配置已更新"));

                // 标记方块实体为已更改，触发保存
                compressor.setChanged();

                // 同步给附近的所有玩家
                NetworkHandler.sendSideConfigSync(level, pos, sideConfig);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}