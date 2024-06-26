package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.common.inventory.slot.ExtremeRecipeSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2SJEIGhostPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public class C2SJEIGhostPacket {
    private final int slotIndex;
    private final ItemStack stack;

    public C2SJEIGhostPacket(FriendlyByteBuf buf) {
        this.slotIndex = buf.readInt();
        this.stack = buf.readItem();
    }
    public C2SJEIGhostPacket(int slotIndex, ItemStack stack) {
        this.slotIndex = slotIndex;
        this.stack = stack;
    }

    public static void write(C2SJEIGhostPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slotIndex);
        buf.writeItem(msg.stack);
    }

    public static void run(C2SJEIGhostPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && msg.slotIndex >= 0 && msg.slotIndex < player.containerMenu.slots.size()
                    && player.containerMenu.getSlot(msg.slotIndex) instanceof ExtremeRecipeSlot f
            )
            {
                f.set(msg.stack);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
