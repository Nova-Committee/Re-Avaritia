package committee.nova.mods.avaritia.common.net.chest;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/28 12:44
 * @Description:
 */
public enum ChannelAction {
    ADD(0, "add"),
    REMOVE(1, "remove"),
    SET(2, "set");
    private final String name;
    private final int id;

    ChannelAction(int id, String name) {
        this.name = name;
        this.id = id;
    }

    public static final StreamCodec<FriendlyByteBuf, ChannelAction> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ChannelAction decode(@NotNull FriendlyByteBuf buffer) {
            return buffer.readEnum(ChannelAction.class);
        }

        @Override
        public void encode(@NotNull FriendlyByteBuf buffer, @NotNull ChannelAction channelAction) {
            buffer.writeEnum(channelAction);

        }
    };
}
