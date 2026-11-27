package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.common.net.PacketContext;
import committee.nova.mods.avaritia.api.iface.IDataReceiver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Supplier;

/** Menu-scoped data updates; server authority remains with the active validated receiver. */
public final class NbtDataPack {
    public final CompoundTag tag;
    public NbtDataPack(CompoundTag tag) { this.tag = tag.copy(); }
    public NbtDataPack(FriendlyByteBuf buffer) {
        tag = buffer.readNbt();
        if (tag == null) throw new IllegalArgumentException("Missing menu data");
    }
    public void write(FriendlyByteBuf buffer) { buffer.writeNbt(tag); }
    public void run(Supplier<PacketContext> context) {
        context.get().enqueueWork(() -> {
            var player = context.get().getSender();
            if (player == null) ClientPacketProxy.handle(this);
            else if (player.containerMenu.stillValid(player) && player.containerMenu instanceof IDataReceiver receiver) {
                receiver.receive(tag);
                player.containerMenu.broadcastChanges();
            }
        });
        context.get().setPacketHandled(true);
    }
}
