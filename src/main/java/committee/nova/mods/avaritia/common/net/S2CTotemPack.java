package committee.nova.mods.avaritia.common.net;




import net.minecraft.network.FriendlyByteBuf;


import net.minecraft.world.item.ItemStack;


import committee.nova.mods.avaritia.api.common.net.PacketContext;

import java.util.function.Supplier;

/**
 * S2CTotemPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public class S2CTotemPack {
    public final ItemStack stack;
    public final int entityId;

    public S2CTotemPack(FriendlyByteBuf buf) {
        this.stack = buf.readItem();
        this.entityId = buf.readInt();
    }

    public S2CTotemPack(ItemStack stack, int entityId) {
        this.stack = stack;
        this.entityId = entityId;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeItem(this.stack);
        buf.writeInt(this.entityId);
    }

    public void run(Supplier<PacketContext> ctx) { ctx.get().enqueueWork(() -> committee.nova.mods.avaritia.common.net.ClientPacketProxy.handle(this)); ctx.get().setPacketHandled(true); }
}
