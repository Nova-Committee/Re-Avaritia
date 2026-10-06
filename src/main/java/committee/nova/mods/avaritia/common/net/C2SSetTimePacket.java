package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.item.misc.InfinityClockTimes;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Applies Infinity Clock TimeUp selections on the server.
 */
public record C2SSetTimePacket(int time) implements CustomPacketPayload {
    public static final Type<C2SSetTimePacket> TYPE = new Type<>(Const.rl("c2s_set_time"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SSetTimePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            C2SSetTimePacket::time,
            C2SSetTimePacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SSetTimePacket> {
        @Override
        public void handle(@NotNull C2SSetTimePacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player)  {
                    var server = player.level().getServer();
                    ServerClockManager clockManager = server.clockManager();
                    Set<Holder<WorldClock>> updatedClocks = new HashSet<>();

                    server.getAllLevels().forEach(level -> {
                        Holder<DimensionType> dimensionType = level.dimensionTypeRegistration();
                        dimensionType.value().defaultClock().ifPresent(clockHolder -> {
                            if (updatedClocks.add(clockHolder)) {
                                long currentTicks = clockManager.getTotalTicks(clockHolder);
                                clockManager.setTotalTicks(clockHolder, InfinityClockTimes.resolveSelectedDayTime(currentTicks, packet.time()));
                            }
                        });
                    });
                }

            });
        }
    }


}
