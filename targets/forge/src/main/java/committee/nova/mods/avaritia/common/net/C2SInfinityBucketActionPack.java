package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.InfinityBucketMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Forge 1.20 {@code ServerboundContainerButtonClickPacket} stores buttonId as a byte.
 * Infinity Bucket action ids are 1000–6000+, so they must travel as full ints.
 */
public class C2SInfinityBucketActionPack {
    private final int containerId;
    private final int actionId;

    public C2SInfinityBucketActionPack(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.actionId = buf.readInt();
    }

    public C2SInfinityBucketActionPack(int containerId, int actionId) {
        this.containerId = containerId;
        this.actionId = actionId;
    }

    public int containerId() {
        return containerId;
    }

    public int actionId() {
        return actionId;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeInt(actionId);
    }

    public void run(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            if (!(player.containerMenu instanceof InfinityBucketMenu menu) || menu.containerId != containerId) {
                return;
            }
            if (!menu.stillValid(player)) {
                Const.LOGGER.debug("Player {} interacted with invalid infinity bucket menu {}", player, menu);
                return;
            }
            menu.clickMenuButton(player, actionId);
        });
        context.get().setPacketHandled(true);
    }
}
