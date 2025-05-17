package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/25 19:29
 * @Description:
 */
public class RefinedCoalItem extends ResourceItem{
    public RefinedCoalItem(String registryName) {
        super(ModRarities.UNCOMMON, registryName, true, new Properties().stacksTo(32));
    }

    public static final int BURN_TIME = 16000 * 10;

    @Override
    public int getBurnTime(@NotNull ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return BURN_TIME;
    }
}
