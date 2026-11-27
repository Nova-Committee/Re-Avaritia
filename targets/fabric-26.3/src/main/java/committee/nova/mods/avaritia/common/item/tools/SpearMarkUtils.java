package committee.nova.mods.avaritia.common.item.tools;

import committee.nova.mods.avaritia.api.iface.SpearMarkEntityData;
import committee.nova.mods.avaritia.common.component.SpearMark;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class SpearMarkUtils {
    private SpearMarkUtils() {
    }

    public static SpearMark apply(LivingEntity target, Player owner) {
        SpearMark mark = new SpearMark(owner.getUUID(), target.level().getGameTime() + SpearMark.DURATION_TICKS);
        ((SpearMarkEntityData) target).avaritia$setSpearMark(mark);
        return mark;
    }

    public static boolean isMarkedBy(LivingEntity target, Player owner) {
        SpearMark mark = getActiveMark(target);
        return mark != null && mark.isOwnedBy(owner.getUUID(), target.level().getGameTime());
    }

    public static @Nullable SpearMark getActiveMark(LivingEntity target) {
        if (!(target instanceof SpearMarkEntityData data)) {
            return null;
        }
        SpearMark mark = data.avaritia$getSpearMark();
        return mark != null && mark.isActive(target.level().getGameTime()) ? mark : null;
    }
}
