package committee.nova.mods.avaritia.common.component;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

/**
 * 中子战魂铠的归属标记：谁第一次骑上/召回了这只善良恶魂，谁就是它的主人。
 *
 * @param ownerId 主人 UUID，{@link #NO_OWNER} 表示尚未认主
 */
public record HarnessOwner(UUID ownerId) {
    public static final UUID NO_OWNER = new UUID(0L, 0L);
    public static final HarnessOwner EMPTY = new HarnessOwner(NO_OWNER);

    public static final MapCodec<HarnessOwner> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(HarnessOwner::ownerId)
    ).apply(instance, HarnessOwner::new));

    public boolean isUnowned() {
        return this.ownerId.equals(NO_OWNER);
    }

    public boolean isOwnedBy(UUID playerId) {
        return !isUnowned() && this.ownerId.equals(playerId);
    }
}
