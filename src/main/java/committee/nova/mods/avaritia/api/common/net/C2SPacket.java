package committee.nova.mods.avaritia.api.common.net;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/**
 * Name: Avaritia-fabric / C2SPacket
 * Author: cnlimiter
 * CreateTime: 2023/9/17 19:08
 * Description:
 */

public interface C2SPacket extends Packet {
    /**
     * 此方法将在网络线程上运行。大多数方法调用应该通过在服务器线程上将代码包装在 lambda 中来执行:
     * <pre>
     * <code>
     *     server.execute(() -> {
     * 	    // code here
     *  })
     * </code>
     * </pre>
     */
    void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener, PacketSender responseSender, SimpleChannel channel);
}
