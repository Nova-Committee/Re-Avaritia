package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.UUID;

/** Stable item-side reference to a server-owned infinity chest channel. */
public record InfinityChestReference(UUID owner, boolean locked, String filter, byte sortType, UUID channelId) {
    private static final int MAX_FILTER_LENGTH = 64;
    private static final Codec<String> FILTER_CODEC = Codec.STRING.validate(value -> value.length() <= MAX_FILTER_LENGTH
            ? DataResult.success(value)
            : DataResult.error(() -> "Infinity chest filter is longer than " + MAX_FILTER_LENGTH + " characters"));

    public static final Codec<InfinityChestReference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(InfinityChestReference::owner),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(InfinityChestReference::locked),
            FILTER_CODEC.optionalFieldOf("filter", "").forGetter(InfinityChestReference::filter),
            Codec.BYTE.optionalFieldOf("sortType", (byte) 4).forGetter(InfinityChestReference::sortType),
            UUIDUtil.CODEC.fieldOf("channelID").forGetter(InfinityChestReference::channelId)
    ).apply(instance, InfinityChestReference::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfinityChestReference> STREAM_CODEC = StreamCodec.of(
            InfinityChestReference::encode,
            InfinityChestReference::decode
    );

    public InfinityChestReference {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(channelId, "channelId");
        filter = filter == null ? "" : filter;
        if (filter.length() > MAX_FILTER_LENGTH) {
            filter = filter.substring(0, MAX_FILTER_LENGTH);
        }
        if (sortType < 0 || sortType > 7) {
            sortType = 4;
        }
    }

    private static void encode(RegistryFriendlyByteBuf buffer, InfinityChestReference reference) {
        buffer.writeUUID(reference.owner);
        buffer.writeBoolean(reference.locked);
        buffer.writeUtf(reference.filter, MAX_FILTER_LENGTH);
        buffer.writeByte(reference.sortType);
        buffer.writeUUID(reference.channelId);
    }

    private static InfinityChestReference decode(RegistryFriendlyByteBuf buffer) {
        return new InfinityChestReference(buffer.readUUID(), buffer.readBoolean(), buffer.readUtf(MAX_FILTER_LENGTH),
                buffer.readByte(), buffer.readUUID());
    }
}
