package committee.nova.mods.avaritia.api.iface;

import committee.nova.mods.avaritia.common.component.SpearMark;
import org.jspecify.annotations.Nullable;

/**
 * 由 LivingEntitySpearMarkMixin 挂载到 LivingEntity 的长矛标记数据（替代 Neo DataAttachment）。
 */
public interface SpearMarkEntityData {
    @Nullable
    SpearMark avaritia$getSpearMark();

    void avaritia$setSpearMark(@Nullable SpearMark mark);
}
