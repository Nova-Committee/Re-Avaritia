package committee.nova.mods.avaritia.api.client.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * Name: Avaritia-forge / AlphaOverrideVertexConsumer
 * Author: cnlimiter
 * CreateTime: 2023/9/24 22:41
 * Description:
 */

public class AlphaOverrideVertexConsumer implements VertexConsumer {
    private final int alpha;
    protected final VertexConsumer delegate;

    public AlphaOverrideVertexConsumer(VertexConsumer delegate, int alpha) {
        this.delegate = delegate;
        this.alpha = alpha;
    }

    public AlphaOverrideVertexConsumer(VertexConsumer delegate, double alpha) {
        this.delegate = delegate;
        this.alpha = (int) (255 * alpha);
    }

    @Override
    public @NotNull VertexConsumer vertex(double x, double y, double z) {
        delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public @NotNull VertexConsumer color(int r, int g, int b, int a) {
        delegate.color(r, g, b, alpha);
        return this;
    }

    @Override
    public @NotNull VertexConsumer uv(float u, float v) {
        delegate.uv(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer overlayCoords(int u, int v) {
        delegate.overlayCoords(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer uv2(int u, int v) {
        delegate.uv2(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer normal(float x, float y, float z) {
        delegate.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        delegate.defaultColor(r, g, b, a);
    }


    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }
}
