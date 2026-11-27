package committee.nova.mods.avaritia.api.client.render.buffer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link VertexConsumer} implementation which forwards to a delegate.
 * <p>
 * Created by covers1624 on 29/3/22.
 */
public abstract class DelegatingVertexConsumer implements ISpriteAwareVertexConsumer {

    protected final VertexConsumer delegate;

    public DelegatingVertexConsumer(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void sprite(TextureAtlasSprite sprite) {
        if (delegate instanceof ISpriteAwareVertexConsumer spriteCons) {
            spriteCons.sprite(sprite);
        }
    }

    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
        delegate.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        delegate.setNormal(normalX, normalY, normalZ);
        return this;
    }
}
