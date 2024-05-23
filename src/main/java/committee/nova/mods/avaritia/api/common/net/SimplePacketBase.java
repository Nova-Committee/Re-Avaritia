package committee.nova.mods.avaritia.api.common.net;


import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
/**
 * Name: Avaritia-fabric / SimplePacketBase
 * Author: cnlimiter
 * CreateTime: 2023/9/15 0:42
 * Description:
 */

public abstract class SimplePacketBase implements C2SPacket, S2CPacket {

    public abstract void write(FriendlyByteBuf buffer);

    public abstract boolean handle(Context context);

    @Override
    public final void encode(FriendlyByteBuf buffer) {
        write(buffer);
    }

    @Override
    public void handle(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
        handle(new Context(client, listener, null));
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener, PacketSender responseSender, SimpleChannel channel) {
        handle(new Context(server, listener, player));
    }

    public enum NetworkDirection {
        PLAY_TO_CLIENT,
        PLAY_TO_SERVER
    }

    public record Context(Executor exec, PacketListener listener, @Nullable ServerPlayer sender) {
        public void enqueueWork(Runnable runnable) {
            exec().execute(runnable);
        }

        @Nullable
        public ServerPlayer getSender() {
            return sender();
        }

        public NetworkDirection getDirection() {
            return sender() == null ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT;
        }
    }
}
