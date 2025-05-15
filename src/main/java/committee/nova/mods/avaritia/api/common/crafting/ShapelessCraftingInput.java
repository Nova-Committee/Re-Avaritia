package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/14 19:15
 * @Description:
 */
public class ShapelessCraftingInput extends CraftingInput {
    public ShapelessCraftingInput(List<ItemStack> items) {
        super(items.size(), 1, items);
    }
}
