package committee.nova.mods.avaritia.api.common.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/10/12 23:00
 * @Description:
 */
public interface ITierCraftingRecipe extends Recipe<TierInput> {

    public int getTier();

    public boolean hasRequiredTier();

    default @NotNull NonNullList<ItemStack> getRemainingItems(TierInput inventory) {
        var remaining = NonNullList.withSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); ++i) {
            var item = inventory.getItem(i);
            if (item.hasCraftingRemainingItem()) {
                remaining.set(i, item.getCraftingRemainingItem());
            }
        }
        return remaining;
    }
}
