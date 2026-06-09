package committee.nova.mods.avaritia.common.net.chest;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * 压缩箱频道状态。
 */
public enum ChannelState {
    COMMON(0, "common"),
    FULL(1, "full");

    private final int id;
    private final String name;

    ChannelState(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getSerializedName() {
        return this.name;
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
