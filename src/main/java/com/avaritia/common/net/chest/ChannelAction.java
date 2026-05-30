package com.avaritia.common.net.chest;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * 压缩箱频道操作类型。
 */
public enum ChannelAction {
    ADD(0, "add"),
    REMOVE(1, "remove"),
    SET(2, "set");

    private final int id;
    private final String name;

    ChannelAction(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getSerializedName() {
        return this.name;
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
