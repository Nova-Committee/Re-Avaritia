package committee.nova.mods.avaritia.common.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SSetTimePacket {
    private final int time;

    public C2SSetTimePacket(int time) {
        this.time = time;
    }

    public C2SSetTimePacket(FriendlyByteBuf buf) {
        this.time = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(time);
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getServer().getAllLevels().forEach(level -> level.setDayTime(time));
        });
        ctx.get().setPacketHandled(true);
    }
}
