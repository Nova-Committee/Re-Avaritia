package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.common.dimension.InfinityRingDimensions;
import committee.nova.mods.avaritia.common.dimension.InfinityRingSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Client actions for Infinity Ring create/control screens. */
public class C2SInfinityRingPack {
    public static final int CREATE = 0;
    public static final int SET_MODES = 1;
    public static final int ADD_FRIEND = 2;
    public static final int REMOVE_FRIEND = 3;
    public static final int VISIT = 4;
    public static final int DELETE = 5;
    public static final int SET_ROLE = 6;
    public static final int BAN = 7;
    public static final int UNBAN = 8;

    private final int action;
    private final int terrain;
    private final int time;
    private final int weather;
    private final int access;
    private final String name;
    private final UUID owner;

    public C2SInfinityRingPack(int action, int terrain, int time, int weather, int access, String name, UUID owner) {
        this.action = action;
        this.terrain = terrain;
        this.time = time;
        this.weather = weather;
        this.access = access;
        this.name = name;
        this.owner = owner;
    }

    public static C2SInfinityRingPack of(int action, int terrain, int time, int weather, int access, String name, UUID owner) {
        return new C2SInfinityRingPack(action, terrain, time, weather, access, name, owner);
    }

    public C2SInfinityRingPack(FriendlyByteBuf buf) {
        this.action = buf.readVarInt();
        this.terrain = buf.readVarInt();
        this.time = buf.readVarInt();
        this.weather = buf.readVarInt();
        this.access = buf.readVarInt();
        this.name = buf.readUtf();
        this.owner = buf.readUUID();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(action);
        buf.writeVarInt(terrain);
        buf.writeVarInt(time);
        buf.writeVarInt(weather);
        buf.writeVarInt(access);
        buf.writeUtf(name);
        buf.writeUUID(owner);
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !InfinityRingDimensions.holdingRing(player)) {
                return;
            }
            switch (action) {
                case CREATE -> InfinityRingDimensions.create(player,
                        InfinityRingSettings.Terrain.byId(terrain),
                        InfinityRingSettings.TimeMode.byId(time),
                        InfinityRingSettings.WeatherMode.byId(weather),
                        InfinityRingSettings.Access.byId(access));
                case SET_MODES -> InfinityRingDimensions.updateModes(player, owner,
                        InfinityRingSettings.TimeMode.byId(time),
                        InfinityRingSettings.WeatherMode.byId(weather),
                        InfinityRingSettings.Access.byId(access));
                case ADD_FRIEND -> InfinityRingDimensions.addFriend(player, owner, name);
                case REMOVE_FRIEND -> InfinityRingDimensions.removeFriend(player, owner, name);
                case VISIT -> InfinityRingDimensions.visit(player, name);
                case DELETE -> InfinityRingDimensions.deleteOwn(player);
                case SET_ROLE -> InfinityRingDimensions.setRole(player, owner, name,
                        InfinityRingSettings.Role.byId(access));
                case BAN -> InfinityRingDimensions.ban(player, owner, name);
                case UNBAN -> InfinityRingDimensions.unban(player, owner, name);
                default -> {
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
