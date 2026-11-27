package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.common.net.PacketContext;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSpawnPoint;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

/** Server reply tied to one request and one owner's control screen. */
public record S2CInfinityRingSpawnPack(UUID owner, UUID request, Result result, @Nullable InfinityRingSpawnPoint spawn) {
    public enum Result {
        STATUS, SAVED, DENIED, UNSAFE
    }

    public S2CInfinityRingSpawnPack(FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readUUID(), buffer.readEnum(Result.class), buffer.readNullable(buf ->
                new InfinityRingSpawnPoint(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat())));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(owner);
        buffer.writeUUID(request);
        buffer.writeEnum(result);
        buffer.writeNullable(spawn, (buf, point) -> {
            buf.writeDouble(point.x());
            buf.writeDouble(point.y());
            buf.writeDouble(point.z());
            buf.writeFloat(point.yaw());
            buf.writeFloat(point.pitch());
        });
    }

    public static S2CInfinityRingSpawnPack reply(C2SInfinityRingSpawnPack request, Result result,
                                                @Nullable InfinityRingSpawnPoint spawn) {
        return new S2CInfinityRingSpawnPack(request.owner(), request.request(), result, spawn);
    }

    public void run(Supplier<PacketContext> context) {
        ClientPacketProxy.infinityRingSpawn.accept(this);
    }
}
