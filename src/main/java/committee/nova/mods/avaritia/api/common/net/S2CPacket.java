package committee.nova.mods.avaritia.api.common.net;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

/**
 * Name: Avaritia-fabric / S2CPacket
 * Author: cnlimiter
 * CreateTime: 2023/9/17 19:10
 * Description:
 */

public interface S2CPacket extends Packet {
    /**
     * 此方法将在网络线程上运行。大多数方法调用应通过将代码包装在 lambda 中来在客户端线程上执行:
     * <pre>
     * <code>
     *     client.execute(() -> {
     *  	// code here
     *  })
     * </code>
     * </pre>
     */
    void handle(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel);
}
