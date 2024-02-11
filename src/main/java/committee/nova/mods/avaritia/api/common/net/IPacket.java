package committee.nova.mods.avaritia.api.common.net;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:57
 * Version: 1.0
 */
public interface IPacket<T extends IPacket<T>> extends CustomPacketPayload {
    public abstract void run(T msg, PlayPayloadContext ctx);
}
