package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

/** Requests the current entry point or saves the server-side standing position. */
public record C2SInfinityRingSpawnPack(UUID owner, UUID request, boolean save) {
    public C2SInfinityRingSpawnPack(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readUUID(), buffer.readBoolean());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(owner);
        buffer.writeUUID(request);
        buffer.writeBoolean(save);
    }

    public void run(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && NetworkHandler.RING_SPAWN.isRemotePresent(context.getNetworkManager())) {
                NetworkHandler.RING_SPAWN.send(PacketDistributor.PLAYER.with(() -> player),
                        InfinityRingDimensions.handleSpawnRequest(player, this));
            }
        });
        context.setPacketHandled(true);
    }
}
