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
public class C2SInfinityChestActionPack {
    private final int containerId;
    private final int actionId;
    private final String id;

    public C2SInfinityChestActionPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.actionId = buf.readInt();
        this.id = buf.readUtf();
    }

    public C2SInfinityChestActionPack(int containerId, int actionId, String object) {
        this.containerId = containerId;
        this.actionId = actionId;
        this.id = object;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeInt(actionId);
        buf.writeUtf(id);
    }

    public void run(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId != containerId) return;
            if (!player.containerMenu.stillValid(player)) {
                Const.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            } else {
                ((InfinityChestMenu) player.containerMenu).action(actionId, id);
                player.containerMenu.broadcastChanges();
            }
        });
        context.get().setPacketHandled(true);
    }
}
