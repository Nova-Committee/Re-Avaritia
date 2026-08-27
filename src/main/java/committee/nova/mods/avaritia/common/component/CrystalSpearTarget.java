package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
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
        SpearTargetReference reference = SpearTargetReference.of(target);
        return new CrystalSpearTarget(
                reference.targetId(),
                reference.dimension(),
                reference.chunkX(),
                reference.chunkZ(),
                MAX_THRUSTS);
    }

    public boolean matches(LivingEntity target) {
        return this.reference().matches(target);
    }

    public @Nullable LivingEntity resolve(Level level) {
        return this.reference().resolve(level);
    }

    public ChunkPos lastKnownChunk() {
        return this.reference().lastKnownChunk();
    }

    public CrystalSpearTarget refreshPosition(LivingEntity target) {
        SpearTargetReference currentReference = this.reference();
        if (!currentReference.matches(target)) {
            return of(target);
        }
        SpearTargetReference refreshedReference = currentReference.refreshPosition(target);
        if (refreshedReference == currentReference) {
            return this;
        }
        return new CrystalSpearTarget(
                refreshedReference.targetId(),
                refreshedReference.dimension(),
                refreshedReference.chunkX(),
                refreshedReference.chunkZ(),
                this.remainingThrusts);
    }

    public @Nullable CrystalSpearTarget consumeThrust() {
        if (this.remainingThrusts == 1) {
            return null;
        }
        return new CrystalSpearTarget(
                this.targetId, this.dimension, this.chunkX, this.chunkZ, this.remainingThrusts - 1);
    }

    static int blockToChunk(int coordinate) {
        return SpearTargetReference.blockToChunk(coordinate);
    }

    private SpearTargetReference reference() {
        return new SpearTargetReference(this.targetId, this.dimension, this.chunkX, this.chunkZ);
    }
}
