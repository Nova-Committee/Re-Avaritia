package com.avaritia.compat.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.util.IntBounds;

import java.util.List;

/**
 * KubeJS 配方 schema 的小型工具方法。
 */
public final class KjsUtils {
    private KjsUtils() {
    }

    public static <T> RecipeKey<List<T>> optionalList(RecipeComponent<T> component, String name, ComponentRole role) {
        return component.asConditionalList()
                .orSelf()
                .withBounds(IntBounds.OPTIONAL)
                .key(name, role)
                .optional(List.of());
    }
}
