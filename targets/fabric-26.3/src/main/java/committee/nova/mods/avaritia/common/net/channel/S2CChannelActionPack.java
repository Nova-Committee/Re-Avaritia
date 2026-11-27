package committee.nova.mods.avaritia.common.net.channel;


import net.minecraft.network.FriendlyByteBuf;


import committee.nova.mods.avaritia.api.common.net.PacketContext;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/28 12:34
 * @Description:
 */
public class S2CChannelActionPack {
    public final ChannelAction action;
    public final byte type;
    public final String name;
    public final int id;

    public S2CChannelActionPack(FriendlyByteBuf buf) {
        this.action = buf.readEnum(ChannelAction.class);
        this.type = buf.readByte();
        this.name = buf.readUtf();
        this.id = buf.readInt();
    }

    public S2CChannelActionPack(ChannelAction action, byte type, String name, int id) {
        this.action = action;
        this.type = type;
        this.name = name;
        this.id = id;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeByte(type);
        buf.writeUtf(name);
        buf.writeInt(id);
    }

    public void run(Supplier<PacketContext> context) { context.get().enqueueWork(() -> committee.nova.mods.avaritia.common.net.ClientPacketProxy.handle(this)); context.get().setPacketHandled(true); }
}
