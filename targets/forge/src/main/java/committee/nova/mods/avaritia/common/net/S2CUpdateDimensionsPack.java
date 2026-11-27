package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/** Syncs runtime-created personal dimension keys into the client known-level set before teleport. */
public class S2CUpdateDimensionsPack {
    private final Set<ResourceKey<Level>> keys;
    private final boolean add;

    public S2CUpdateDimensionsPack(Set<ResourceKey<Level>> keys, boolean add) {
        this.keys = keys;
        this.add = add;
    }

    public S2CUpdateDimensionsPack(FriendlyByteBuf buf) {
        this.add = buf.readBoolean();
        int size = buf.readVarInt();
        Set<ResourceKey<Level>> decoded = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            decoded.add(buf.readResourceKey(Registries.DIMENSION));
        }
        this.keys = decoded;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(add);
        buf.writeVarInt(keys.size());
        for (ResourceKey<Level> key : keys) {
            buf.writeResourceKey(key);
        }
    }

    public Set<ResourceKey<Level>> keys() {
        return keys;
    }

    public boolean add() {
        return add;
    }

    public static void addToAll(MinecraftServer server, ResourceKey<Level> key) {
        S2CUpdateDimensionsPack packet = new S2CUpdateDimensionsPack(Set.of(key), true);
        NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void addTo(ServerPlayer player, ResourceKey<Level> key) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new S2CUpdateDimensionsPack(Set.of(key), true));
    }

    public static void removeFromAll(MinecraftServer server, ResourceKey<Level> key) {
        S2CUpdateDimensionsPack packet = new S2CUpdateDimensionsPack(Set.of(key), false);
        NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientPacketProxy.updateDimensions.accept(this));
        ctx.get().setPacketHandled(true);
    }
}
