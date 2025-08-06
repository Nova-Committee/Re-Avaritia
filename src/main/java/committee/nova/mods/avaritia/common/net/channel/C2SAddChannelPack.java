package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.core.channel.ServerChannelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/1 15:05
 * @Description:
 */
public class C2SAddChannelPack {

    private final String name;
    private final boolean pub;

    public C2SAddChannelPack(FriendlyByteBuf buf) {
        this.name = buf.readUtf(64);
        this.pub = buf.readBoolean();
    }

    public C2SAddChannelPack(String name, boolean pub) {
        this.name = name;
        this.pub = pub;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(name, 64);
        buf.writeBoolean(pub);
    }

    public void run(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (name.isEmpty()) return;
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            ServerChannelManager.getInstance().tryAddChannel(player, name, pub);
        });
        context.get().setPacketHandled(true);
    }
}

