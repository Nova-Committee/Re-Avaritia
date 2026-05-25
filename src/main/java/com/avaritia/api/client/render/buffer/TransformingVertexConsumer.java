package com.avaritia.api.client.render.buffer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.avaritia.api.utils.vec.Matrix4;
import com.avaritia.api.utils.vec.Transformation;
import com.avaritia.api.utils.vec.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Created by covers1624 on 4/24/20.
 */
public class TransformingVertexConsumer extends DelegatingVertexConsumer {

    private final Transformation transform;
    private final Vector3 storage = new Vector3();

    public TransformingVertexConsumer(VertexConsumer delegate, PoseStack stack) {
        this(delegate, new Matrix4(stack));
    }

    public TransformingVertexConsumer(VertexConsumer delegate, Transformation transform) {
        super(delegate);
        this.transform = transform;
    }

    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        storage.set(x, y, z);
        transform.apply(storage);
        return super.addVertex((float) storage.x, (float) storage.y, (float) storage.z);
    }

    @Override
    public @NotNull VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        storage.set(normalX, normalY, normalZ);
        transform.applyN(storage);
        return delegate.setNormal((float) storage.x, (float) storage.y, (float) storage.z);
    }
}
