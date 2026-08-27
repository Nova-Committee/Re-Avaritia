package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * 可持久化的长矛目标引用，不包含具体武器的使用次数规则。
 */
public record SpearTargetReference(UUID targetId, UUID ownerId, Identifier dimension,
                                   int chunkX, int chunkZ, long expiresAt) {
    public static final Codec<SpearTargetReference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("target").forGetter(SpearTargetReference::targetId),
            UUIDUtil.CODEC.optionalFieldOf("owner", SpearMark.NO_OWNER).forGetter(SpearTargetReference::ownerId),
            Identifier.CODEC.fieldOf("dimension").forGetter(SpearTargetReference::dimension),
            Codec.INT.fieldOf("chunk_x").forGetter(SpearTargetReference::chunkX),
            Codec.INT.fieldOf("chunk_z").forGetter(SpearTargetReference::chunkZ),
            Codec.LONG.optionalFieldOf("expires_at", 0L).forGetter(SpearTargetReference::expiresAt)
    ).apply(instance, SpearTargetReference::new));

    public static final StreamCodec<ByteBuf, SpearTargetReference> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SpearTargetReference::targetId,
            UUIDUtil.STREAM_CODEC, SpearTargetReference::ownerId,
            Identifier.STREAM_CODEC, SpearTargetReference::dimension,
            ByteBufCodecs.VAR_INT, SpearTargetReference::chunkX,
            ByteBufCodecs.VAR_INT, SpearTargetReference::chunkZ,
            ByteBufCodecs.VAR_LONG, SpearTargetReference::expiresAt,
            SpearTargetReference::new);

    /** 仅供旧版水晶矛目标组件读取；旧目标不会被当作有效标记。 */
    public SpearTargetReference(UUID targetId, Identifier dimension, int chunkX, int chunkZ) {
        this(targetId, SpearMark.NO_OWNER, dimension, chunkX, chunkZ, 0L);
    }

    public static SpearTargetReference of(LivingEntity target) {
        return new SpearTargetReference(
                target.getUUID(), SpearMark.NO_OWNER, target.level().dimension().identifier(),
                blockToChunk(target.getBlockX()), blockToChunk(target.getBlockZ()), 0L);
    }

    public static SpearTargetReference of(LivingEntity target, UUID ownerId, long expiresAt) {
        return new SpearTargetReference(
                target.getUUID(),
                ownerId,
                target.level().dimension().identifier(),
                blockToChunk(target.getBlockX()),
                blockToChunk(target.getBlockZ()),
                expiresAt);
    }

    public boolean matches(LivingEntity target) {
        return this.targetId.equals(target.getUUID());
    }

    public @Nullable LivingEntity resolve(Level level) {
        Entity target = level.getEntityInAnyDimension(this.targetId);
        return target instanceof LivingEntity livingTarget ? livingTarget : null;
    }

    public ChunkPos lastKnownChunk() {
        return new ChunkPos(this.chunkX, this.chunkZ);
    }

    public boolean isOwnedBy(UUID playerId, long gameTime) {
        return this.ownerId.equals(playerId) && this.expiresAt > gameTime;
    }

    public SpearTargetReference refreshPosition(LivingEntity target) {
        return refreshPosition(target, this.expiresAt);
    }

    public SpearTargetReference refreshPosition(LivingEntity target, long expiresAt) {
        if (!this.matches(target)) {
            return of(target, this.ownerId, expiresAt);
        }

        Identifier currentDimension = target.level().dimension().identifier();
        int currentChunkX = blockToChunk(target.getBlockX());
        int currentChunkZ = blockToChunk(target.getBlockZ());
        if (this.dimension.equals(currentDimension)
                && this.chunkX == currentChunkX
                && this.chunkZ == currentChunkZ
                && this.expiresAt == expiresAt) {
            return this;
        }
        return new SpearTargetReference(
                this.targetId, this.ownerId, currentDimension, currentChunkX, currentChunkZ, expiresAt);
    }

    static int blockToChunk(int coordinate) {
        return coordinate >> 4;
    }
}
