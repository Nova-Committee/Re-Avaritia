package committee.nova.mods.avaritia.api.client.model;

import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by covers1624 on 13/02/2017.
 *
 * <p>Backported to 1.21.11: 26.1's {@code BakedQuad} took a {@code BakedQuad.MaterialInfo}
 * bundling the sprite, chunk layer, render type, tint index, shade flag and light
 * emission. The 1.21.11 {@code BakedQuad} record has no such field, so those values are
 * passed individually. The render type is no longer carried per quad — 1.21.11 selects it
 * at the model/layer level — so {@code renderType} is kept only for call-site compatibility.
 */
public class ItemQuadBakery {

    public static final PerspectiveModelState IDENTITY = PerspectiveModelState.IDENTITY;
    private static final float MODEL_UNIT = 1.0F / 16.0F;
    private static final float OVERLAY_INSET = 0.25F;
    private static final float OVERLAY_DEPTH_OFFSET = 0.02F;

    public static List<BakedQuad> bakeItem(TextureAtlasSprite... sprites) {
        return bakeItem(IDENTITY, sprites);
    }

    public static List<BakedQuad> bakeItem(RenderType renderType, TextureAtlasSprite... sprites) {
        return bakeItem(IDENTITY, renderType, 0.0F, sprites);
    }

    /**
     * Bakes a flat item overlay slightly inset from and in front of both generated-item faces.
     * The inset keeps filtered mask edges inside the wrapped item, while the depth offset
     * avoids coplanar rejection without disabling depth testing for the whole effect pipeline.
     */
    public static List<BakedQuad> bakeItemOverlay(RenderType renderType, TextureAtlasSprite... sprites) {
        return bakeItem(IDENTITY, renderType, OVERLAY_DEPTH_OFFSET, sprites);
    }

    public static List<BakedQuad> bakeItem(ModelState state, TextureAtlasSprite... sprites) {
        return bakeItem(state, null, sprites);
    }

    public static List<BakedQuad> bakeItem(ModelState state, RenderType renderType, TextureAtlasSprite... sprites) {
        return bakeItem(state, renderType, 0.0F, sprites);
    }

    private static List<BakedQuad> bakeItem(ModelState state, RenderType renderType, float depthOffset, TextureAtlasSprite... sprites) {
        List<BakedQuad> quads = new LinkedList<>();
        for (int i = 0; i < sprites.length; i++) {
            TextureAtlasSprite sprite = sprites[i];
            quads.add(frontFace(state, sprite, i, depthOffset));
            quads.add(backFace(state, sprite, i, depthOffset));
        }
        return quads;
    }

    private static BakedQuad frontFace(ModelState state, TextureAtlasSprite sprite, int tintIndex, float depthOffset) {
        return new BakedQuad(
                transform(state, OVERLAY_INSET, OVERLAY_INSET, 8.5F + depthOffset),
                transform(state, 16.0F - OVERLAY_INSET, OVERLAY_INSET, 8.5F + depthOffset),
                transform(state, 16.0F - OVERLAY_INSET, 16.0F - OVERLAY_INSET, 8.5F + depthOffset),
                transform(state, OVERLAY_INSET, 16.0F - OVERLAY_INSET, 8.5F + depthOffset),
                uv(sprite, 0.0F, 16.0F),
                uv(sprite, 16.0F, 16.0F),
                uv(sprite, 16.0F, 0.0F),
                uv(sprite, 0.0F, 0.0F),
                tintIndex,
                Direction.SOUTH,
                sprite,
                true,
                0
        );
    }

    private static BakedQuad backFace(ModelState state, TextureAtlasSprite sprite, int tintIndex, float depthOffset) {
        return new BakedQuad(
                transform(state, 16.0F - OVERLAY_INSET, OVERLAY_INSET, 7.5F - depthOffset),
                transform(state, OVERLAY_INSET, OVERLAY_INSET, 7.5F - depthOffset),
                transform(state, OVERLAY_INSET, 16.0F - OVERLAY_INSET, 7.5F - depthOffset),
                transform(state, 16.0F - OVERLAY_INSET, 16.0F - OVERLAY_INSET, 7.5F - depthOffset),
                uv(sprite, 16.0F, 16.0F),
                uv(sprite, 0.0F, 16.0F),
                uv(sprite, 0.0F, 0.0F),
                uv(sprite, 16.0F, 0.0F),
                tintIndex,
                Direction.NORTH,
                sprite,
                true,
                0
        );
    }

    static Vector3fc transform(ModelState state, float x, float y, float z) {
        Matrix4fc matrix = state.transformation().getMatrix();
        // FaceBakery 接收 0..16 的模型坐标，但 BakedQuad 顶点存储的是 0..1 坐标。
        // 这里直接构造覆盖层四边形，因此必须显式执行同样的归一化，否则 mask 会放大 16 倍。
        return matrix.transformPosition(new Vector3f(x, y, z).mul(MODEL_UNIT));
    }

    private static long uv(TextureAtlasSprite sprite, float u, float v) {
        // Cuboid UVs are expressed in the traditional 0..16 model space, while
        // TextureAtlasSprite#getU/getV use a normalized 0..1 offset.
        // Passing 16 directly walks beyond this sprite and samples unrelated
        // entries from the atlas, which makes an item mask appear on other
        // fragments of the generated item plane.
        return UVPair.pack(sprite.getU(u * MODEL_UNIT), sprite.getV(v * MODEL_UNIT));
    }

}
