package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.singularity.Singularity;
import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * S2CSingularitiesPacket
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:58
 * Version: 1.0
 */
public record S2CSingularitiesPacket(Collection<Singularity> dataSingularities,
                                     Collection<Singularity> runSingularities,
                                     Collection<Identifier> removedRecipes,
                                     Collection<Identifier> removedSingularities,
                                     boolean removeAllRecipes,
                                     boolean removeAll) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CSingularitiesPacket> TYPE = new CustomPacketPayload.Type<>(Const.rl("s2c_singularities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSingularitiesPacket> STREAM_CODEC = StreamCodec.of(
            S2CSingularitiesPacket::write,
            S2CSingularitiesPacket::read
    );

    public static S2CSingularitiesPacket from(SingularityReloadListener listener) {
        return new S2CSingularitiesPacket(
                listener.getDataSingularities().values(),
                listener.getRunSingularities().values(),
                listener.getRemoveRecipes(),
                listener.getRemoveSingularities(),
                listener.isRemoveAllRecipes(),
                listener.isRemoveAll()
        );
    }

    private static S2CSingularitiesPacket read(RegistryFriendlyByteBuf buffer) {
        var dataSingularities = readSingularities(buffer);
        var runSingularities = readSingularities(buffer);
        var removedRecipes = readIds(buffer);
        var removedSingularities = readIds(buffer);
        boolean removeAllRecipes = buffer.readBoolean();
        boolean removeAll = buffer.readBoolean();
        return new S2CSingularitiesPacket(dataSingularities, runSingularities, removedRecipes, removedSingularities, removeAllRecipes, removeAll);
    }

    private static void write(RegistryFriendlyByteBuf buffer, S2CSingularitiesPacket packet) {
        writeSingularities(buffer, packet.dataSingularities);
        writeSingularities(buffer, packet.runSingularities);
        writeIds(buffer, packet.removedRecipes);
        writeIds(buffer, packet.removedSingularities);
        buffer.writeBoolean(packet.removeAllRecipes);
        buffer.writeBoolean(packet.removeAll);
    }

    private static ArrayList<Singularity> readSingularities(RegistryFriendlyByteBuf buffer) {
        return ByteBufCodecs.collection(ArrayList::new, Singularity.STREAM_CODEC).decode(buffer);
    }

    private static void writeSingularities(RegistryFriendlyByteBuf buffer, Collection<Singularity> singularities) {
        ByteBufCodecs.collection(ArrayList::new, Singularity.STREAM_CODEC).encode(buffer, new ArrayList<>(singularities));
    }

    private static ArrayList<Identifier> readIds(RegistryFriendlyByteBuf buffer) {
        return ByteBufCodecs.collection(ArrayList::new, Identifier.STREAM_CODEC).decode(buffer);
    }

    private static void writeIds(RegistryFriendlyByteBuf buffer, Collection<Identifier> ids) {
        ByteBufCodecs.collection(ArrayList::new, Identifier.STREAM_CODEC).encode(buffer, new ArrayList<>(ids));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<S2CSingularitiesPacket> {
        @Override
        public void handle(@NotNull S2CSingularitiesPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> SingularityReloadListener.INSTANCE.applySyncedState(
                    packet.dataSingularities,
                    packet.runSingularities,
                    packet.removedRecipes,
                    packet.removedSingularities,
                    packet.removeAllRecipes,
                    packet.removeAll
            ));
        }
    }



}
