package com.avaritia.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeMap;

import java.lang.reflect.Method;

public final class ClientRecipeMaps {
    private ClientRecipeMaps() {
    }

    public static RecipeMap get() {
        RecipeMap jeiRecipes = getJeiClientSyncedRecipes();
        if (!jeiRecipes.values().isEmpty()) {
            return jeiRecipes;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null) {
            return minecraft.getSingleplayerServer().getRecipeManager().recipeMap();
        }

        return RecipeMap.EMPTY;
    }

    private static RecipeMap getJeiClientSyncedRecipes() {
        try {
            Class<?> internal = Class.forName("mezz.jei.common.Internal");
            Method method = internal.getMethod("getClientSyncedRecipes");
            Object recipes = method.invoke(null);
            if (recipes instanceof RecipeMap recipeMap) {
                return recipeMap;
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
        return RecipeMap.EMPTY;
    }
}
