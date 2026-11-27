package committee.nova.mods.avaritia.api.common.caps;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/12/28 20:18
 * @Description:
 */
public interface IFilters {
    List<ItemStack> getAllFilters();
    void addFilter(ItemStack stack);
    void removeFilter(ItemStack stack);
    void removeAll();
}
