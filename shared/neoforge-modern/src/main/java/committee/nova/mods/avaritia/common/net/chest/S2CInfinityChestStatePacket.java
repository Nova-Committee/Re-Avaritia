package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.chest.ChestHandler;
import committee.nova.mods.avaritia.core.chest.ClientChestManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** 服务端到客户端的无尽箱全量或增量镜像。 */
public record S2CInfinityChestStatePacket(
        ChannelState state,
        boolean reset,
        boolean last,
        Collection<ChestHandler.StoredItem> changed,
        Collection<ItemResource> removed) implements CustomPacketPayload {
    public static final int PAGE_SIZE = 512;
    public static final Type<S2CInfinityChestStatePacket> TYPE =
            new Type<>(Const.rl("s2c_infinity_chest_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CInfinityChestStatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ChannelState.STREAM_CODEC, S2CInfinityChestStatePacket::state,
                    ByteBufCodecs.BOOL, S2CInfinityChestStatePacket::reset,
                    ByteBufCodecs.BOOL, S2CInfinityChestStatePacket::last,
                    ByteBufCodecs.collection(ArrayList::new, ChestHandler.STORED_ITEM_STREAM_CODEC,
                            PAGE_SIZE), S2CInfinityChestStatePacket::changed,
                    ByteBufCodecs.collection(ArrayList::new, ItemResource.STREAM_CODEC,
                            PAGE_SIZE), S2CInfinityChestStatePacket::removed,
                    S2CInfinityChestStatePacket::new);

    public S2CInfinityChestStatePacket {
        changed = List.copyOf(changed);
        removed = List.copyOf(removed);
        if (changed.size() > PAGE_SIZE || removed.size() > PAGE_SIZE
                || changed.size() + removed.size() > PAGE_SIZE) {
            throw new IllegalArgumentException("Infinity chest packet exceeds its page limit");
        }
    }

    public static S2CInfinityChestStatePacket fullPage(
            Collection<ChestHandler.StoredItem> entries, boolean reset, boolean last) {
        return new S2CInfinityChestStatePacket(ChannelState.FULL, reset, last, entries, List.of());
    }

    public static S2CInfinityChestStatePacket incremental(
            Collection<ChestHandler.StoredItem> changed, Collection<ItemResource> removed) {
        return new S2CInfinityChestStatePacket(ChannelState.COMMON, false, true, changed, removed);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<S2CInfinityChestStatePacket> {
        @Override
        public void handle(@NotNull S2CInfinityChestStatePacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (packet.state() == ChannelState.FULL) {
                    ClientChestManager.getInstance().fullUpdatePage(
                            packet.changed(), packet.reset(), packet.last());
                } else {
                    ClientChestManager.getInstance().update(packet.changed(), packet.removed());
                }
            });
        }
    }
}
