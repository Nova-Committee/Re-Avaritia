package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.ChannelSelectMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 20:27
 * @Description:
 */
public class C2SSetChannelPack {

    private final int containerId;
    private final byte type;
    private final int id;

    public C2SSetChannelPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.type = buf.readByte();
        this.id = buf.readInt();
    }

    public C2SSetChannelPack(int containerId, byte type, int id) {
        this.containerId = containerId;
        this.type = type;
        this.id = id;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeByte(type);
        buf.writeInt(id);
    }

    public void run(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId != containerId) return;
            if (!player.containerMenu.stillValid(player)) {
                Const.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            } else {
                ((ChannelSelectMenu) player.containerMenu).setChannel(type, id);
            }
        });
        context.get().setPacketHandled(true);
    }
}
