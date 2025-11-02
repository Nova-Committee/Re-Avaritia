package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.iface.ITileIO;
import committee.nova.mods.avaritia.core.io.SideConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 方块配置同步数据包
 * Description: 服务端同步方块配置到客户端
 * @author cnlimiter
 * Date: 2025/11/01
 * Version: 1.0
 */
public class S2CSideConfigSyncPacket {
    private final BlockPos pos;
    private final SideConfiguration sideConfig;

    public S2CSideConfigSyncPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.sideConfig = SideConfiguration.fromNetwork(buf);
    }

    public S2CSideConfigSyncPacket(BlockPos pos, SideConfiguration sideConfig) {
        this.pos = pos;
        this.sideConfig = sideConfig;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        this.sideConfig.toNetwork(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            BlockEntity tile = level.getBlockEntity(this.pos);

            if (tile instanceof ITileIO tileIO) {
                // 应用同步的配置
                tileIO.setSideConfiguration(sideConfig);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}