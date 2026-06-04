package com.avaritia.api.client.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.math.Transformation;
import com.avaritia.api.client.util.TransformUtils;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.Map;

/**
 * A simple {@link ModelState} implementation which
 * is composed of multiple {@link Transformation}s.
 * <p>
 * Created by covers1624 on 9/7/22.
 *
 * @see TransformUtils
 */
public class PerspectiveModelState implements ModelState {

    public static final PerspectiveModelState IDENTITY = new PerspectiveModelState(ImmutableMap.of());
    private static final Transformation IDENTITY_TRANSFORM = Transformation.IDENTITY;

    private final Map<ItemDisplayContext, Transformation> transforms;
    private final boolean isUvLocked;

    public PerspectiveModelState(Map<ItemDisplayContext, Transformation> transforms) {
        this(transforms, false);
    }

    public PerspectiveModelState(Map<ItemDisplayContext, Transformation> transforms, boolean isUvLocked) {
        this.transforms = ImmutableMap.copyOf(transforms);
        this.isUvLocked = isUvLocked;
    }

    public Transformation getTransform(ItemDisplayContext context) {
        return transforms.getOrDefault(context, IDENTITY_TRANSFORM);
    }

    @Override
    public Transformation transformation() {
        return getTransform(ItemDisplayContext.NONE);
    }

    public boolean isUvLocked() {
        return isUvLocked;
    }
}
