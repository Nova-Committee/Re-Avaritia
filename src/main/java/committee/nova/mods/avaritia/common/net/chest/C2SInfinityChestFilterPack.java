package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author: cnlimiter
 */
public class C2SInfinityChestFilterPack {
    private final int containerId;
    private final String filter;

    public C2SInfinityChestFilterPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.filter = buf.readUtf(64);
    }

    public C2SInfinityChestFilterPack(int containerId, String filter) {
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
                    Const.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
                } else {
                    ((InfinityChestMenu) player.containerMenu).filter = filter;
                    player.containerMenu.broadcastChanges();
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
