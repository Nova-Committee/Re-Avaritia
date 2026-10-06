package committee.nova.mods.avaritia.common.net;


import committee.nova.mods.avaritia.core.io.SideConfiguration;


import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;



import committee.nova.mods.avaritia.api.common.net.PacketContext;

import java.util.function.Supplier;

/**
 * 方块配置同步数据包
 * Description: 服务端同步方块配置到客户端
 * @author cnlimiter
 * Date: 2025/11/01
 * Version: 1.0
 */
public class S2CSideConfigSyncPacket {
    public final BlockPos pos;
    public final SideConfiguration sideConfig;

    public S2CSideConfigSyncPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.sideConfig = SideConfiguration.fromNetwork(buf);
    }

    public S2CSideConfigSyncPacket(BlockPos pos, SideConfiguration sideConfig) {
        this.pos = pos;
        this.sideConfig = sideConfig;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        this.sideConfig.toNetwork(buf);
    }

    public void run(Supplier<PacketContext> ctx) { ctx.get().enqueueWork(() -> committee.nova.mods.avaritia.common.net.ClientPacketProxy.handle(this)); ctx.get().setPacketHandled(true); }
}