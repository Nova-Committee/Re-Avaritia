package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.Objects;

public record InfinityBucketCreature(Identifier typeId, CompoundTag entityData) {
    public static final int MAX_NBT_BYTES = InfinityBucketBudget.MAX_CREATURE_BYTES;
    public static final Codec<InfinityBucketCreature> CODEC = RecordCodecBuilder.<InfinityBucketCreature>create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(InfinityBucketCreature::typeId),
            CompoundTag.CODEC.fieldOf("data").forGetter(InfinityBucketCreature::entityData)
    ).apply(instance, InfinityBucketCreature::new)).validate(creature -> {
        int size = InfinityBucketBudget.encodedCreatureSize(creature);
        return size > MAX_NBT_BYTES
                ? com.mojang.serialization.DataResult.error(() -> "Infinity Bucket creature exceeds " + MAX_NBT_BYTES + " bytes")
                : com.mojang.serialization.DataResult.success(creature);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, InfinityBucketCreature> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            InfinityBucketCreature::typeId,
            ByteBufCodecs.COMPOUND_TAG,
            InfinityBucketCreature::entityData,
            InfinityBucketCreature::new
    );

    public InfinityBucketCreature {
        Objects.requireNonNull(typeId, "typeId");
        entityData = entityData == null ? new CompoundTag() : entityData.copy();
    }

    public boolean isOversized() {
        return entityData.sizeInBytes() > MAX_NBT_BYTES;
    }

    public EntityType<?> entityType() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(typeId).orElse(null);
    }

    public Component displayName() {
        if (entityData.contains("CustomName")) {
            try {
                Tag raw = entityData.get("CustomName");
                if (raw != null) {
                    Component parsed = ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, raw).result().orElse(null);
                    if (parsed != null) {
                        return parsed;
                    }
                }
            } catch (RuntimeException ignored) {
            }
            String json = entityData.getStringOr("CustomName", "");
            if (!json.isEmpty()) {
                try {
                    Component parsed = ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, net.minecraft.nbt.StringTag.valueOf(json))
                            .result()
                            .orElse(null);
                    if (parsed != null) {
                        return parsed;
                    }
                } catch (RuntimeException ignored) {
                }
            }
        }
        EntityType<?> type = entityType();
        return type == null ? Component.literal(typeId.toString()) : type.getDescription();
    }
}
