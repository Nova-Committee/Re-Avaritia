package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 双锋破界之矛“七进七出”的每栈冷却状态。
 *
 * @param endsAt 冷却结束时的世界游戏刻
 */
public record CrystalSpearCooldown(long endsAt) {
    public static final int DURATION_TICKS = 60 * 20;

    public static final Codec<CrystalSpearCooldown> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("ends_at").forGetter(CrystalSpearCooldown::endsAt)
    ).apply(instance, CrystalSpearCooldown::new));

    public static final StreamCodec<ByteBuf, CrystalSpearCooldown> STREAM_CODEC =
            ByteBufCodecs.VAR_LONG.map(CrystalSpearCooldown::new, CrystalSpearCooldown::endsAt);

    public static CrystalSpearCooldown start(long gameTime) {
        return new CrystalSpearCooldown(gameTime + DURATION_TICKS);
    }

    public boolean isActive(long gameTime) {
        return this.endsAt > gameTime;
    }

    public long remainingTicks(long gameTime) {
        return Math.max(0L, this.endsAt - gameTime);
    }

    public long remainingSeconds(long gameTime) {
        return (remainingTicks(gameTime) + 19L) / 20L;
    }
}
