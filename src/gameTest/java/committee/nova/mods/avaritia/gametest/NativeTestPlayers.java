package committee.nova.mods.avaritia.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/** Real vanilla player and packet listener for in-world regression fixtures. */
public final class NativeTestPlayers {
    private NativeTestPlayers() {}

    public static ServerPlayer get(ServerLevel level, GameProfile profile) {
        ServerPlayer player = new ServerPlayer(level.getServer(), level, profile);
        new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player);
        return player;
    }
}
