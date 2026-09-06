package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Syncs runtime-created personal dimension keys into the client known-level set before teleport. */
public record S2CUpdateDimensionsPack(Set<ResourceKey<Level>> keys, boolean add) implements CustomPacketPayload {
    public static final Type<S2CUpdateDimensionsPack> TYPE = new Type<>(Const.rl("s2c_update_dimensions"));
    public static final StreamCodec<ByteBuf, S2CUpdateDimensionsPack> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION).apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf),
            S2CUpdateDimensionsPack::keys,
            ByteBufCodecs.BOOL,
            S2CUpdateDimensionsPack::add,
            S2CUpdateDimensionsPack::new);

    public static void addToAll(MinecraftServer server, ResourceKey<Level> key) {
        S2CUpdateDimensionsPack packet = new S2CUpdateDimensionsPack(Set.of(key), true);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(player, packet);
        }
    }

    public static void addTo(ServerPlayer player, ResourceKey<Level> key) {
        PacketDistributor.sendToPlayer(player, new S2CUpdateDimensionsPack(Set.of(key), true));
    }

    public static void removeFromAll(MinecraftServer server, ResourceKey<Level> key) {
        S2CUpdateDimensionsPack packet = new S2CUpdateDimensionsPack(Set.of(key), false);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(player, packet);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<S2CUpdateDimensionsPack> {
        @Override
        public void handle(@NotNull S2CUpdateDimensionsPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player == null) {
                    return;
                }
                Set<ResourceKey<Level>> levels = player.connection.levels();
                if (levels == null) {
                    return;
                }
                Consumer<ResourceKey<Level>> op = packet.add ? levels::add : levels::remove;
                packet.keys.forEach(op);
            });
        }
    }
}
