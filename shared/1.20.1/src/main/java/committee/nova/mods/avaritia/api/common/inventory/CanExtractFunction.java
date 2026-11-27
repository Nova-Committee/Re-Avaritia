package committee.nova.mods.avaritia.api.common.inventory;

import net.minecraft.core.Direction;

/**
 * @author cnlimiter
 */
@FunctionalInterface
public interface CanExtractFunction {
    boolean apply(int i);

    @FunctionalInterface
    public interface Sided {
        boolean apply(int i, Direction direction);
    }
}
