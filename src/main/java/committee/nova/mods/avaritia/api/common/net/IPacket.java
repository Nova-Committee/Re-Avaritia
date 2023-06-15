package committee.nova.mods.avaritia.api.common.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:57
 * Version: 1.0
 */
public abstract class IPacket<T extends IPacket<T>> {

    public IPacket() {
    }

    public abstract T read(FriendlyByteBuf buf);

    public abstract void write(T msg, FriendlyByteBuf buf);

    public abstract void run(T msg, Supplier<NetworkEvent.Context> ctx);
}
