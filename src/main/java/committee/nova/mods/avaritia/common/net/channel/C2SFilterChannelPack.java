package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.addons._channel.ChannelMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 14:00
 * @Description:
 */
public class C2SFilterChannelPack {

    private final int containerId;
    private final String filter;

    public C2SFilterChannelPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.filter = buf.readUtf(64);
    }

    public C2SFilterChannelPack(int containerId, String filter) {
        this.containerId = containerId;
        this.filter = filter;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeUtf(filter, 64);
    }

    public void run(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId == containerId) {
                if (!player.containerMenu.stillValid(player)) {
                    Static.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
                } else {
                    ((ChannelMenu) player.containerMenu).filter = filter;
                    player.containerMenu.broadcastChanges();
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
