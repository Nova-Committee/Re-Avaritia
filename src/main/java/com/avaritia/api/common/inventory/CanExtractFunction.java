package com.avaritia.api.common.inventory;

import net.minecraft.core.Direction;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/5/14 19:12
 * @Description:
 */
@FunctionalInterface
public interface CanExtractFunction {
    boolean apply(int i);

    @FunctionalInterface
    interface Sided {
        boolean apply(int i, Direction direction);
    }
}
