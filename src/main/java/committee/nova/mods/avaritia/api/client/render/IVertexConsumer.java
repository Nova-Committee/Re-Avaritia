package committee.nova.mods.avaritia.api.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import committee.nova.mods.avaritia.util.client.render.VertexUtil;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/**
 * Author cnlimiter
 * CreateTime 2023/9/24 23:19
 * Name IVertexConsumer
 * Description
 */

public interface IVertexConsumer {
    VertexFormat getVertexFormat();

    void setQuadTint(int tint);

    void setQuadOrientation(Direction orientation);

    void setApplyDiffuseLighting(boolean diffuse);

    void setTexture(TextureAtlasSprite texture);

    void put(int element, float... data);

    /**
     * Assumes the data is already completely unpacked.
     * You must always copy the data from the quad provided to an internal cache.
     * basically:
     * this.quad.put(quad);
     *
     * @param quad The quad to copy data from.
     */
    void put(Quad quad);

    default void put(BakedQuad quad) {
        VertexUtil.putQuad(this, quad);
    }
}
