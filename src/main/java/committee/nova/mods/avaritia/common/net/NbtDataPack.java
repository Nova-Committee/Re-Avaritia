package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.iface.IDataReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/23 01:45
 * @Description:
 */
public record NbtDataPack(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NbtDataPack> TYPE = new CustomPacketPayload.Type<>(Static.rl("sync_nbt"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NbtDataPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            NbtDataPack::tag,
            NbtDataPack::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<NbtDataPack> {
        @Override
        public void handle(@NotNull NbtDataPack packet, @NotNull IPayloadContext context) {
            if(context.flow() == PacketFlow.CLIENTBOUND) {
                context.enqueueWork(() -> {
                    Player sender = context.player();
                    if (sender != null && sender.containerMenu instanceof IDataReceiver dataReceiver) {
                        dataReceiver.receive(packet.tag);
                    }
                });
            } else if(context.flow() == PacketFlow.SERVERBOUND) {
                context.enqueueWork(() -> {
                    if(Minecraft.getInstance().screen instanceof IDataReceiver dataReceiver) {
                        dataReceiver.receive(packet.tag());
                    }
                });
            }
        }
    }
}
