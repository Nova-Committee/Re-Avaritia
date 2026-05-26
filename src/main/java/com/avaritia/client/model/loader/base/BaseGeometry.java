package com.avaritia.client.model.loader.base;

import net.minecraft.client.renderer.block.model.BlockModel;

/**
 * @author cnlimiter
 */
public abstract class BaseGeometry<U> {
    public final BlockModel baseModel;

    public BaseGeometry(BlockModel baseModel) {
        this.baseModel = baseModel;
    }
}
