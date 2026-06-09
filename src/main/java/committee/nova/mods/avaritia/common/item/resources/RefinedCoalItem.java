package committee.nova.mods.avaritia.common.item.resources;

import committee.nova.mods.avaritia.init.registry.ModItems;

import committee.nova.mods.avaritia.init.registry.ModRarities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/25 19:29
 * @Description:
 */
public class RefinedCoalItem extends ResourceItem{
    public RefinedCoalItem() {
        super(ModRarities.UNCOMMON, true, ModItems.properties().stacksTo(32));
    }

    public static final int BURN_TIME = 16000 * 10;

    @Override
    public int getBurnTime(@NonNull ItemStack itemStack, @Nullable RecipeType<?> recipeType, @NonNull FuelValues fuelValues) {
        return BURN_TIME;
    }

}
