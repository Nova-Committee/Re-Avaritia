package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 中子战魂铠的立体防御状态：护盾结束时刻与技能冷却结束时刻（世界游戏刻）。
 *
 * @param shieldUntil   护盾失效时的世界游戏刻
 * @param cooldownUntil 立体防御冷却结束时的世界游戏刻
 */
public record HarnessShieldState(long shieldUntil, long cooldownUntil) {
    public static final HarnessShieldState EMPTY = new HarnessShieldState(0L, 0L);

    public static final MapCodec<HarnessShieldState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.LONG.fieldOf("shield_until").forGetter(HarnessShieldState::shieldUntil),
            Codec.LONG.fieldOf("cooldown_until").forGetter(HarnessShieldState::cooldownUntil)
    ).apply(instance, HarnessShieldState::new));

    public boolean isShieldActive(long gameTime) {
        return this.shieldUntil > gameTime;
    }

    public boolean isCoolingDown(long gameTime) {
        return this.cooldownUntil > gameTime;
    }
}
