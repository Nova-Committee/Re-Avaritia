package com.avaritia.client.compat;

import net.minecraft.world.item.ItemDisplayContext;

public class IrisCompat {

    private static Boolean IRIS_LOADED;

    public static boolean isShaderPackEnabled() {
        try {
            Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");

            Object api = irisApi.getMethod("getInstance").invoke(null);

            return (boolean) irisApi
                    .getMethod("isShaderPackInUse")
                    .invoke(api);

        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean shouldDefer(ItemDisplayContext ctx) {

        if (!isShaderPackEnabled()) {
            return false;
        }

        return switch (ctx) {

            case FIRST_PERSON_LEFT_HAND,
                 FIRST_PERSON_RIGHT_HAND,
                 THIRD_PERSON_LEFT_HAND,
                 THIRD_PERSON_RIGHT_HAND -> true;

            default -> false;
        };
    }
}