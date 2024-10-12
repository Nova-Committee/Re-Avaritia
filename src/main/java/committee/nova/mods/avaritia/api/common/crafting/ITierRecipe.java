package committee.nova.mods.avaritia.api.common.crafting;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/10/12 23:00
 * @Description:
 */
public interface ITierRecipe {

    public int getTier();

    public boolean hasRequiredTier();
}
