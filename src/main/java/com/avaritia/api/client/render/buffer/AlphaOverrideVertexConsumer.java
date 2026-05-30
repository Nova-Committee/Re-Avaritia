package com.avaritia.api.client.render.buffer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * Created by covers1624 on 29/3/22.
 */
public class AlphaOverrideVertexConsumer extends DelegatingVertexConsumer {

    private final int alpha;

    public AlphaOverrideVertexConsumer(VertexConsumer delegate, double alpha) {
        this(delegate, (int) (255 * alpha));
    }

    public AlphaOverrideVertexConsumer(VertexConsumer delegate, int alpha) {
        super(delegate);
        this.alpha = alpha;
    }

    @Override
    public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return super.setColor(red, green, blue, this.alpha);
    }

    @Override
    public @NotNull VertexConsumer setColor(int color) {
        return super.setColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, this.alpha);
    }

    @Override
    public @NotNull VertexConsumer setLineWidth(float width) {
        delegate.setLineWidth(width);
        return this;
    }
}
