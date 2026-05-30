package com.avaritia.common.item.resources;

import com.avaritia.init.registry.ModRarities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/18 17:30
 * Version: 1.0
 */
public class StarFuelItem extends ResourceItem {


    public static final int BURN_TIME = Integer.MAX_VALUE;

    public StarFuelItem() {
        super(ModRarities.RARE, true, new Properties().stacksTo(16));
    }

    @Override
    public int getBurnTime(@NonNull ItemStack itemStack, @Nullable RecipeType<?> recipeType, @NonNull FuelValues fuelValues) {
        return BURN_TIME;
    }

}
