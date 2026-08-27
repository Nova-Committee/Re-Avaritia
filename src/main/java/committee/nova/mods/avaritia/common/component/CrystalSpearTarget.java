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
 * 双锋破界之矛的持久化锁定信息。
 *
 * @param targetId  目标实体 UUID
 * @param dimension 目标最后一次处于加载状态时的维度
 * @param chunkX    目标最后一次处于加载状态时的区块 X
 * @param chunkZ    目标最后一次处于加载状态时的区块 Z
 * @param remainingThrusts 当前锁定剩余的成功突刺次数
 */
public record CrystalSpearTarget(UUID targetId, Identifier dimension, int chunkX, int chunkZ, int remainingThrusts) {
    public static final int MAX_THRUSTS = 14;

    public static final Codec<CrystalSpearTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("target").forGetter(CrystalSpearTarget::targetId),
            Identifier.CODEC.fieldOf("dimension").forGetter(CrystalSpearTarget::dimension),
            Codec.INT.fieldOf("chunk_x").forGetter(CrystalSpearTarget::chunkX),
            Codec.INT.fieldOf("chunk_z").forGetter(CrystalSpearTarget::chunkZ),
            Codec.intRange(1, MAX_THRUSTS).fieldOf("remaining_thrusts")
                    .forGetter(CrystalSpearTarget::remainingThrusts)
    ).apply(instance, CrystalSpearTarget::new));

    public static final StreamCodec<ByteBuf, CrystalSpearTarget> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, CrystalSpearTarget::targetId,
            Identifier.STREAM_CODEC, CrystalSpearTarget::dimension,
            ByteBufCodecs.VAR_INT, CrystalSpearTarget::chunkX,
            ByteBufCodecs.VAR_INT, CrystalSpearTarget::chunkZ,
            ByteBufCodecs.VAR_INT, CrystalSpearTarget::remainingThrusts,
            CrystalSpearTarget::new);

    public CrystalSpearTarget {
        if (remainingThrusts < 1 || remainingThrusts > MAX_THRUSTS) {
            throw new IllegalArgumentException("remainingThrusts must be between 1 and " + MAX_THRUSTS);
        }
    }

    public static CrystalSpearTarget of(LivingEntity target) {
        return new CrystalSpearTarget(
                target.getUUID(),
                target.level().dimension().identifier(),
                blockToChunk(target.getBlockX()),
                blockToChunk(target.getBlockZ()),
                MAX_THRUSTS);
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

    public CrystalSpearTarget refreshPosition(LivingEntity target) {
        if (!this.matches(target)) {
            return of(target);
        }

        Identifier currentDimension = target.level().dimension().identifier();
        int currentChunkX = blockToChunk(target.getBlockX());
        int currentChunkZ = blockToChunk(target.getBlockZ());
        if (this.dimension.equals(currentDimension)
                && this.chunkX == currentChunkX
                && this.chunkZ == currentChunkZ) {
            return this;
        }
        return new CrystalSpearTarget(
                this.targetId, currentDimension, currentChunkX, currentChunkZ, this.remainingThrusts);
    }

    public @Nullable CrystalSpearTarget consumeThrust() {
        if (this.remainingThrusts == 1) {
            return null;
        }
        return new CrystalSpearTarget(
                this.targetId, this.dimension, this.chunkX, this.chunkZ, this.remainingThrusts - 1);
    }

    static int blockToChunk(int coordinate) {
        return coordinate >> 4;
    }
}
