package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

/**
 * S2CSingularitiesPacket
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
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
                    player.level().getServer().getAllLevels().forEach(level -> {
                        Holder<DimensionType> dimensionType = level.dimensionTypeRegistration();
                        if ( dimensionType.value().defaultClock().isPresent()) {
                            Holder<WorldClock> clockHolder = dimensionType.value().defaultClock().get();
                            ServerClockManager clockManager = level.getServer().clockManager();
                            clockManager.addTicks(clockHolder, packet.time);
                        } else {
                            player.sendOverlayMessage(Component.translatableEscape("commands.time.no_default_clock", dimensionType.getRegisteredName()));
                        }
                    });
                }

            });
        }
    }



}
