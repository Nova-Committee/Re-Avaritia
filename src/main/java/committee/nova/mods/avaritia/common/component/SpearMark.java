package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/**
 * 长矛施加在生物上的临时标记。
 *
 * @param ownerId   当前标记者 UUID
 * @param expiresAt 标记到期时的世界游戏刻
 */
public record SpearMark(UUID ownerId, long expiresAt) {
    public static final int DURATION_TICKS = 30 * 20;
    public static final UUID NO_OWNER = new UUID(0L, 0L);
    public static final SpearMark EMPTY = new SpearMark(NO_OWNER, 0L);

    public static final MapCodec<SpearMark> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(SpearMark::ownerId),
            Codec.LONG.fieldOf("expires_at").forGetter(SpearMark::expiresAt)
    ).apply(instance, SpearMark::new));

    public static final StreamCodec<ByteBuf, SpearMark> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SpearMark::ownerId,
            ByteBufCodecs.VAR_LONG, SpearMark::expiresAt,
            SpearMark::new);

    public boolean isActive(long gameTime) {
        return !this.ownerId.equals(NO_OWNER) && this.expiresAt > gameTime;
    }

    public boolean isOwnedBy(UUID playerId, long gameTime) {
        return this.ownerId.equals(playerId) && isActive(gameTime);
    }

    public long remainingTicks(long gameTime) {
        return Math.max(0L, this.expiresAt - gameTime);
    }
}
