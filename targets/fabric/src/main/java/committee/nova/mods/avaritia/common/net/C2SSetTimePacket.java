package committee.nova.mods.avaritia.common.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import committee.nova.mods.avaritia.api.common.net.PacketContext;

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

    public void run(Supplier<PacketContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || time < 0 || time >= 24000
                    || !(player.containerMenu instanceof committee.nova.mods.avaritia.common.menu.InfinityClockMenu)
                    || !player.containerMenu.stillValid(player)
                    || !committee.nova.mods.avaritia.Const.findInventoryItem(player,
                    stack -> stack.is(committee.nova.mods.avaritia.init.registry.ModItems.infinity_clock.get()), false, stack -> true)) return;

            player.getServer().getAllLevels().forEach(level -> {
                long currentTime = level.getDayTime();
                long currentDay = currentTime / 24000L;
                long newTime = currentDay * 24000L + time;
                level.setDayTime(newTime);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
