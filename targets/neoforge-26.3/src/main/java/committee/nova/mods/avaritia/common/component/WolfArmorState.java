package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 中子战狼铠的机制状态：羁绊（替主人挡下致命一击）的冷却终点。
 * <p>不死图腾不再走冷却，改为消耗铠甲耐久，因此这里只保留一条时间。</p>
 *
 * @param bondReadyAt 羁绊冷却结束时的世界游戏刻
 */
public record WolfArmorState(long bondReadyAt) {
    public static final WolfArmorState EMPTY = new WolfArmorState(0L);

    public static final MapCodec<WolfArmorState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.LONG.fieldOf("bond_ready_at").forGetter(WolfArmorState::bondReadyAt)
    ).apply(instance, WolfArmorState::new));

    public boolean isBondReady(long gameTime) {
        return gameTime >= this.bondReadyAt;
    }

    public WolfArmorState withBondCooldown(long gameTime, long cooldownTicks) {
        return new WolfArmorState(gameTime + cooldownTicks);
    }
}
