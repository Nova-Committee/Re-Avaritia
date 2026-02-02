package committee.nova.mods.avaritia.api.common.inventory;

/**
 * @author cnlimiter
 */
@FunctionalInterface
public interface OnContentsChangedFunction {
    void apply(int i);
}
