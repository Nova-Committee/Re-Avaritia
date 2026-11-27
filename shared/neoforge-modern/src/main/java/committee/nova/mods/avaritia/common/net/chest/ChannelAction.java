package committee.nova.mods.avaritia.common.net.chest;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/** 无尽箱虚拟槽支持的全部交互。 */
public enum ChannelAction {
    LEFT_CLICK,
    RIGHT_CLICK,
    LEFT_SHIFT,
    RIGHT_SHIFT,
    THROW_ONE,
    THROW_STACK,
    LEFT_DRAG,
    RIGHT_DRAG,
    CLONE,
    DRAG_CLONE;

    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelAction> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ChannelAction decode(@NotNull RegistryFriendlyByteBuf buffer) {
            int ordinal = buffer.readVarInt();
            if (ordinal < 0 || ordinal >= values().length) {
                throw new DecoderException("Invalid infinity chest action: " + ordinal);
            }
            return values()[ordinal];
        }

        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull ChannelAction action) {
            buffer.writeVarInt(action.ordinal());
        }
    };
}
