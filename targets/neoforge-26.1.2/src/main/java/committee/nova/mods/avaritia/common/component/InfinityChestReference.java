package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/** 物品与方块实体携带的稳定无限箱通道引用。 */
public record InfinityChestReference(UUID owner, boolean locked, String filter, byte sortType, UUID channelId) {
    public static final int MAX_FILTER_LENGTH = 64;
    public static final byte DEFAULT_SORT_TYPE = 4;

    private static final Codec<String> FILTER_CODEC = Codec.STRING.validate(value ->
            value.length() <= MAX_FILTER_LENGTH
                    ? DataResult.success(value)
                    : DataResult.error(() -> "Infinity chest filter is longer than " + MAX_FILTER_LENGTH + " characters"));

    public static final Codec<InfinityChestReference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(InfinityChestReference::owner),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(InfinityChestReference::locked),
            FILTER_CODEC.optionalFieldOf("filter", "").forGetter(InfinityChestReference::filter),
            Codec.BYTE.optionalFieldOf("sortType", DEFAULT_SORT_TYPE).forGetter(InfinityChestReference::sortType),
            UUIDUtil.CODEC.fieldOf("channelID").forGetter(InfinityChestReference::channelId)
    ).apply(instance, InfinityChestReference::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfinityChestReference> STREAM_CODEC = StreamCodec.of(
            InfinityChestReference::encode,
            InfinityChestReference::decode
    );

    public InfinityChestReference {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(channelId, "channelId");
        filter = sanitizeFilter(filter);
        if (sortType < 0 || sortType > 7) {
            sortType = DEFAULT_SORT_TYPE;
        }
    }

    public static String sanitizeFilter(String filter) {
        if (filter == null || filter.isEmpty()) {
            return "";
        }
        return filter.substring(0, Math.min(MAX_FILTER_LENGTH, filter.length()));
    }

    public static boolean canModify(UUID owner, boolean locked, UUID candidate) {
        return owner != null && candidate != null && (!locked || owner.equals(candidate));
    }

    /** 读取旧方块物品中使用 chestID/channelId 别名的独立引用。 */
    public static @Nullable InfinityChestReference readLegacyTag(CompoundTag input) {
        CompoundTag tag = input.copy();
        if (!tag.contains("channelID")) {
            String legacyKey = tag.contains("chestID") ? "chestID" : tag.contains("channelId") ? "channelId" : null;
            if (legacyKey != null) {
                Tag channel = tag.get(legacyKey);
                if (channel != null) tag.put("channelID", channel.copy());
            }
        }
        return CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(null);
    }

    private static void encode(RegistryFriendlyByteBuf buffer, InfinityChestReference reference) {
        buffer.writeUUID(reference.owner);
        buffer.writeBoolean(reference.locked);
        buffer.writeUtf(reference.filter, MAX_FILTER_LENGTH);
        buffer.writeByte(reference.sortType);
        buffer.writeUUID(reference.channelId);
    }

    private static InfinityChestReference decode(RegistryFriendlyByteBuf buffer) {
        return new InfinityChestReference(
                buffer.readUUID(),
                buffer.readBoolean(),
                buffer.readUtf(MAX_FILTER_LENGTH),
                buffer.readByte(),
                buffer.readUUID()
        );
    }
}
