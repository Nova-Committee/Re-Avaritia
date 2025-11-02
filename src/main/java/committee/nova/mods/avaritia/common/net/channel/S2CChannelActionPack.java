package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.core.channel.ClientChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/28 12:34
 * @Description:
 */
public class S2CChannelActionPack {
    private final ChannelAction action;
    private final byte type;
    private final String name;
    private final int id;

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

    public void run(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            switch (action) {
                case ADD -> ClientChannelManager.getInstance().addChannel(type, id, name);
                case REMOVE -> ClientChannelManager.getInstance().removeChannel(type, id, name);
                case SET -> ClientChannelManager.getInstance().setSelectedChannel(type, id, name);
            }
        }));
        context.get().setPacketHandled(true);
    }
}
