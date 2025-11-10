package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author cnlimiter
 */
public class C2SInfinityChestActionPack {
    private final int containerId;
    private final int actionId;
    private final ItemStack item;

    public C2SInfinityChestActionPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.actionId = buf.readInt();
        this.item = buf.readItem();
    }

    public C2SInfinityChestActionPack(int containerId, int actionId, ItemStack item) {
        this.containerId = containerId;
        this.actionId = actionId;
        this.item = item;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeInt(actionId);
        buf.writeItem(item);
    }

    public void run(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (player.containerMenu.containerId != containerId) return;
            if (!player.containerMenu.stillValid(player)) {
                Const.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            } else {
                ((InfinityChestMenu) player.containerMenu).action(actionId, item);
                player.containerMenu.broadcastChanges();
            }
        });
        context.get().setPacketHandled(true);
    }
}
