package com.avaritia.api.common.crafting;

import net.minecraft.world.item.crafting.Recipe;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/10/12 23:00
 * @Description:
 */
public interface ITierCraftingRecipe extends Recipe<TierInput> {

    public int getTier();

    public boolean hasRequiredTier();
}
