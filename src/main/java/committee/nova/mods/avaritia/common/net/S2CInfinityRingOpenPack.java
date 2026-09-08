package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Opens Infinity Ring create or control screens on the client. */
public class S2CInfinityRingOpenPack {
    private final boolean create;
    private final int terrain;
    private final int time;
    private final int weather;
    private final int access;
    private final List<String> friends;
    private final UUID owner;
    private final boolean canDelete;

    public S2CInfinityRingOpenPack(boolean create, int terrain, int time, int weather, int access,
                                   List<String> friends, UUID owner, boolean canDelete) {
        this.create = create;
        this.terrain = terrain;
        this.time = time;
        this.weather = weather;
        this.access = access;
        this.friends = friends;
        this.owner = owner;
        this.canDelete = canDelete;
    }

    public S2CInfinityRingOpenPack(FriendlyByteBuf buf) {
        this.create = buf.readBoolean();
        this.terrain = buf.readVarInt();
        this.time = buf.readVarInt();
        this.weather = buf.readVarInt();
        this.access = buf.readVarInt();
        int size = buf.readVarInt();
        List<String> names = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            names.add(buf.readUtf());
        }
        this.friends = names;
        this.owner = buf.readUUID();
        this.canDelete = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(create);
        buf.writeVarInt(terrain);
        buf.writeVarInt(time);
        buf.writeVarInt(weather);
        buf.writeVarInt(access);
        buf.writeVarInt(friends.size());
        for (String friend : friends) {
            buf.writeUtf(friend);
        }
        buf.writeUUID(owner);
        buf.writeBoolean(canDelete);
    }

    public boolean create() {
        return create;
    }

    public int terrain() {
        return terrain;
    }

    public int time() {
        return time;
    }

    public int weather() {
        return weather;
    }

    public int access() {
        return access;
    }

    public List<String> friends() {
        return friends;
    }

    public UUID owner() {
        return owner;
    }

    public boolean canDelete() {
        return canDelete;
    }

    public static void openCreate(ServerPlayer player) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new S2CInfinityRingOpenPack(true, 0, InfinityRingSettings.TimeMode.DAY.ordinal(),
                        InfinityRingSettings.WeatherMode.CLEAR.ordinal(),
                        InfinityRingSettings.Access.PRIVATE.ordinal(), List.of(), player.getUUID(), true));
    }

    public static void openControl(ServerPlayer player, InfinityRingSettings settings, List<String> friends,
                                   UUID owner, boolean canDelete) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new S2CInfinityRingOpenPack(false, settings.terrain.ordinal(), settings.time.ordinal(),
                        settings.weather.ordinal(), settings.access.ordinal(), friends, owner, canDelete));
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientPacketProxy.infinityRingOpen.accept(this));
        ctx.get().setPacketHandled(true);
    }
}
