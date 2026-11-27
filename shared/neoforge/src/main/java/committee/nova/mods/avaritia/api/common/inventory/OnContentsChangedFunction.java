package committee.nova.mods.avaritia.api.common.inventory;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/14 19:13
 * @Description:
 */
@FunctionalInterface
public interface OnContentsChangedFunction {
    void apply(int i);
}
