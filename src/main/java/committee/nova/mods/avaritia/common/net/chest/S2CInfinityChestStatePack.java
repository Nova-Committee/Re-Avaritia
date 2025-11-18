package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.chest.ClientChestManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
public record S2CInfinityChestStatePack(ChannelState channelState, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<S2CInfinityChestStatePack> TYPE = new Type<>(Const.rl("c2s_infinity_chest_State"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CInfinityChestStatePack> STREAM_CODEC = StreamCodec.composite(
            ChannelState.STREAM_CODEC,
            S2CInfinityChestStatePack::channelState,
            ByteBufCodecs.COMPOUND_TAG,
            S2CInfinityChestStatePack::tag,
            S2CInfinityChestStatePack::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<S2CInfinityChestStatePack> {
        @Override
        public void handle(@NotNull S2CInfinityChestStatePack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                switch (packet.channelState) {
                    case COMMON -> ClientChestManager.getInstance().updateChest(packet.tag);
                    case FULL -> ClientChestManager.getInstance().fullUpdateChest(packet.tag);
                    case NAME -> ClientChestManager.getInstance().setUserCache(packet.tag);
                }
            });
        }
    }



}
