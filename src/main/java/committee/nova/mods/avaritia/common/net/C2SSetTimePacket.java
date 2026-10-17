package committee.nova.mods.avaritia.common.net;

import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import committee.nova.mods.avaritia.api.common.net.PacketContext;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class C2SSetTimePacket {
    private static final long DAY_TICKS = 24000L;

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

            MinecraftServer server = player.level().getServer();
            if (server == null) return;
            ServerClockManager clockManager = server.clockManager();
            Set<Holder<WorldClock>> updatedClocks = new HashSet<>();
            server.getAllLevels().forEach(level -> level.dimensionTypeRegistration().value().defaultClock()
                    .ifPresent(clockHolder -> {
                        if (updatedClocks.add(clockHolder)) {
                            long currentTicks = clockManager.getInstance(clockHolder).totalTicks();
                            long currentDay = currentTicks / DAY_TICKS;
                            clockManager.setTotalTicks(clockHolder, currentDay * DAY_TICKS + time);
                        }
                    }));
        });
        ctx.get().setPacketHandled(true);
    }
}
