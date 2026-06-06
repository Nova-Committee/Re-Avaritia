package com.avaritia.api.utils.vec.uv;

import com.avaritia.api.utils.vec.IrreversibleTransformationException;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class MultiIconTransformation extends UVTransformation {

    public TextureAtlasSprite[] icons;

    public MultiIconTransformation(TextureAtlasSprite... icons) {
        this.icons = icons;
    }

    public MultiIconTransformation(MultiIconTransformation other) {
        this(other.icons.clone());
    }

    @Override
    public void apply(UV uv) {
        TextureAtlasSprite icon = icons[uv.tex % icons.length];
        uv.u = icon.getU((float) (uv.u * 16));
        uv.v = icon.getV((float) (uv.v * 16));
    }

    @Override
    public UVTransformation inverse() {
        throw new IrreversibleTransformationException(this);
    }

    @Override
    public MultiIconTransformation copy() {
        return new MultiIconTransformation(this);
    }
}
