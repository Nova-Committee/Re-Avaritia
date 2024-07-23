package committee.nova.mods.avaritia.common.item.resources;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/18 17:30
 * Version: 1.0
 */
public final class StarFuelItem extends ResourceItem {
    public static final int BURN_TIME = Integer.MAX_VALUE;

    public StarFuelItem() {
        super(Rarity.UNCOMMON, "star_fuel", true, new Properties().stacksTo(1));
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return BURN_TIME;
    }
}
