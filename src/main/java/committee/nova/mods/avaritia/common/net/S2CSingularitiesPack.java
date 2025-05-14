package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.common.item.singularity.Singularity;
import committee.nova.mods.avaritia.init.handler.SingularityRegistryHandler;
import committee.nova.mods.avaritia.init.registry.ModSingularities;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * S2CSingularitiesPacket
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public record S2CSingularitiesPack(List<Singularity> singularities) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSingularitiesPack> TYPE = new CustomPacketPayload.Type<>(Static.rl("s2c_singularities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSingularitiesPack> STREAM_CODEC = StreamCodec.composite(
            new StreamCodec<RegistryFriendlyByteBuf, Singularity>() {
                @Override
                public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull Singularity singularity) {
                    RegistryFriendlyByteBuf tmpBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(),
                            buffer.registryAccess(), ConnectionType.NEOFORGE);
                    try {
                        Singularity.streamCodec().encode(tmpBuf, singularity);
                    } catch (Throwable throwable) {
                        tmpBuf.release();
                        buffer.writeBoolean(false);
                        Static.LOGGER.debug("Failed to encode singularity: %s".formatted(singularity), throwable);
                    }
                }

                @Override
                public @NotNull Singularity decode(@NotNull RegistryFriendlyByteBuf buffer) {
                    boolean success = buffer.readBoolean();
                    if (!success) {
                        return ModSingularities.NULL;
                    }
                    RegistryFriendlyByteBuf tmpBuf = new RegistryFriendlyByteBuf(
                            Unpooled.wrappedBuffer(RegistryFriendlyByteBuf.readByteArray(buffer)),
                            buffer.registryAccess(), ConnectionType.NEOFORGE);
                    try {
                        return Singularity.streamCodec().decode(tmpBuf);
                    } catch (Throwable throwable) {
                        return ModSingularities.NULL;
                    } finally {
                        tmpBuf.release();
                    }
                }
            }
    .apply(ByteBufCodecs.<RegistryFriendlyByteBuf, Singularity, Collection<Singularity>>collection(ArrayList::new))
            .map(list -> {
                return list.stream().filter(Objects::nonNull).toList();
            }, UnaryOperator.identity()), S2CSingularitiesPack::singularities, S2CSingularitiesPack::new);




    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<S2CSingularitiesPack> {
        @Override
        public void handle(@NotNull S2CSingularitiesPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                SingularityRegistryHandler.getInstance().loadSingularities(packet);
            });
        }
    }



}
