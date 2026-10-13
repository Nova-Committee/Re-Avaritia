package committee.nova.mods.avaritia.api.util.vec.uv;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class IconTransformation extends UVTransformation {

    public TextureAtlasSprite icon;

    public IconTransformation(TextureAtlasSprite icon) {
        this.icon = icon;
    }

    public IconTransformation(IconTransformation other) {
        this(other.icon);
    }

    @Override
    public void apply(UV uv) {
        uv.u = icon.getU((float) (uv.u * 16));
        uv.v = icon.getV((float) (uv.v * 16));
    }

    @Override
    public UVTransformation inverse() {
        return new Inverse(icon);
    }

    @Override
    public IconTransformation copy() {
        return new IconTransformation(this);
    }

    private static class Inverse extends IconTransformation {

        public Inverse(TextureAtlasSprite icon) {
            super(icon);
        }

        public Inverse(Inverse other) {
            super(other);
        }

        @Override
        public void apply(UV uv) {
            float du = icon.getU1() - icon.getU0();
            float dv = icon.getV1() - icon.getV0();
            uv.u = ((float) uv.u - icon.getU0()) / du / 16;
            uv.v = ((float) uv.v - icon.getV0()) / dv / 16;
        }

        @Override
        public UVTransformation inverse() {
            return new IconTransformation(icon);
        }

        @Override
        public IconTransformation copy() {
            return new Inverse(this);
        }
    }
}
