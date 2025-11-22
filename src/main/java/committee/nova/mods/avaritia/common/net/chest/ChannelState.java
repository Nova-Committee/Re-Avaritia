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
public enum ChannelState {


    COMMON(0, "common"),
    FULL(1, "full");
    private final String name;
    private final int id;

    ChannelState(int id, String name) {
        this.name = name;
        this.id = id;
    }

    public static final StreamCodec<FriendlyByteBuf, ChannelState> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ChannelState decode(@NotNull FriendlyByteBuf buffer) {
            return buffer.readEnum(ChannelState.class);
        }

        @Override
        public void encode(@NotNull FriendlyByteBuf buffer, @NotNull ChannelState channelState) {
            buffer.writeEnum(channelState);

        }
    };
}
