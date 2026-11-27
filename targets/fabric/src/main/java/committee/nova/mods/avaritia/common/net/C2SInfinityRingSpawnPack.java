package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.common.net.PacketContext;
import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

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

    public void run(Supplier<PacketContext> context) {
        ServerPlayer player = context.get().getSender();
        // This request proves the client has registered the optional channel.
        NetworkHandler.RING_SPAWN.sendTo(player, InfinityRingDimensions.handleSpawnRequest(player, this));
    }
}
