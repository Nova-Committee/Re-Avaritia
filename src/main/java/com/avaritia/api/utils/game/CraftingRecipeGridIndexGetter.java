package com.avaritia.api.utils.game;

import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 12:15
 * @Description:
 */
public class CraftingRecipeGridIndexGetter {
    private final ArrayList<Integer> indexArray = new ArrayList<>();
    private int last = 0;

    public CraftingRecipeGridIndexGetter(CraftingRecipe craftingRecipe) {
        if (craftingRecipe instanceof ShapedRecipe recipe) {
            int x = 0;
            int y;
            if (recipe.getWidth() == 1) x = 1;
            if (recipe.getHeight() == 1) y = 1;
            else y = 3 - recipe.getHeight();
            for (int i = y; i < recipe.getHeight() + y; i++) {
                for (int j = x; j < recipe.getWidth() + x; j++) {
                    indexArray.add(i * 3 + j);
                }
            }
        } else {
            if (craftingRecipe.getIngredients().size() == 1) indexArray.add(4);
            else for (int i = 0; i < craftingRecipe.getIngredients().size(); i++) {
                indexArray.add(i);
            }
        }
    }

    public int get() {
        if (last >= indexArray.size()) return 8;
        return indexArray.get(last++);
    }
}
