package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.iface.IDataReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/23 01:45
 * @Description:
 */
public class NbtDataPack {
    public CompoundTag tag;

    public NbtDataPack(CompoundTag tag) {
        this.tag = tag;
    }

    public NbtDataPack(FriendlyByteBuf pb) {
        tag = pb.readAnySizeNbt();
    }

    public void write(FriendlyByteBuf pb) {
        pb.writeNbt(tag);
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer sender = ctx.get().getSender();
                if (sender != null && sender.containerMenu instanceof IDataReceiver dataReceiver) {
                    dataReceiver.receive(tag);
                }
            });
        } else if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {
                if (Minecraft.getInstance().screen instanceof IDataReceiver dataReceiver) {
                    dataReceiver.receive(tag);
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
