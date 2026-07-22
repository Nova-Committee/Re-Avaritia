package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.component.InfinityChestReference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/** 保存当前菜单筛选文本；长度在编解码两端都限制为 64。 */
public record C2SInfinityChestFilterPacket(int containerId, String filter) implements CustomPacketPayload {
    private static final StreamCodec<RegistryFriendlyByteBuf, String> FILTER_CODEC = StreamCodec.of(
            (buffer, value) -> buffer.writeUtf(InfinityChestReference.sanitizeFilter(value),
                    InfinityChestReference.MAX_FILTER_LENGTH),
            buffer -> buffer.readUtf(InfinityChestReference.MAX_FILTER_LENGTH));
    public static final Type<C2SInfinityChestFilterPacket> TYPE =
            new Type<>(Const.rl("c2s_infinity_chest_filter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SInfinityChestFilterPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, C2SInfinityChestFilterPacket::containerId,
                    FILTER_CODEC, C2SInfinityChestFilterPacket::filter,
                    C2SInfinityChestFilterPacket::new);

    public C2SInfinityChestFilterPacket {
        filter = InfinityChestReference.sanitizeFilter(filter);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final class Handler implements IPayloadHandler<C2SInfinityChestFilterPacket> {
        @Override
        public void handle(@NotNull C2SInfinityChestFilterPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                var menu = InfinityChestPayloadGuard.menu(context, packet.containerId());
                if (menu != null) {
                    menu.setFilterFromClient(packet.filter());
                }
            });
        }
    }
}
