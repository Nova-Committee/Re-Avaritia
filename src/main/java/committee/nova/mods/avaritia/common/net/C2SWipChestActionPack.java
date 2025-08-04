package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.ChannelMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 12:24
 * @Description:
 */
public class C2SWipChestActionPack {
    private final int containerId;
    private final int actionId;
    private final String type;
    private final String id;

    public C2SWipChestActionPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.actionId = buf.readInt();
        this.type = buf.readUtf();
        this.id = buf.readUtf();
    }

    public C2SWipChestActionPack(int containerId, int actionId, String[] object) {
        this.containerId = containerId;
        this.actionId = actionId;
        this.type = object[0];
        this.id = object[1];
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeInt(actionId);
        buf.writeUtf(type);
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
                ((ChannelMenu) player.containerMenu).action(actionId, type, id);
                player.containerMenu.broadcastChanges();
            }
        });
        context.get().setPacketHandled(true);
    }
}
