package committee.nova.mods.avaritia.api.client.model;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by covers1624 on 13/02/2017.
 */
public class ItemQuadBakery {

    public static final PerspectiveModelState IDENTITY = PerspectiveModelState.IDENTITY;
    private static final float MODEL_UNIT = 1.0F / 16.0F;
    private static final float OVERLAY_DEPTH_OFFSET = 0.02F;

    public static List<BakedQuad> bakeItem(TextureAtlasSprite... sprites) {
        return bakeItem(IDENTITY, sprites);
    }

    public static List<BakedQuad> bakeItem(RenderType renderType, TextureAtlasSprite... sprites) {
        return bakeItem(IDENTITY, renderType, 0.0F, sprites);
    }

    /**
     * Bakes a flat item overlay slightly in front of both generated-item faces.
     * The offset avoids coplanar depth rejection without changing the X/Y model
     * footprint or disabling depth testing for the whole effect pipeline.
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
        // LambdaUtils.checkArgument(sprites, "Sprites must not be Null or empty!", ArrayUtils::isNullOrContainsNull);

        List<BakedQuad> quads = new LinkedList<>();
        for (int i = 0; i < sprites.length; i++) {
            TextureAtlasSprite sprite = sprites[i];
            BakedQuad.MaterialInfo materialInfo = new BakedQuad.MaterialInfo(
                    sprite,
                    ChunkSectionLayer.TRANSLUCENT,
                    renderType != null ? renderType : RenderTypes.itemTranslucent(sprite.atlasLocation()),
                    i,
                    true,
                    0
            );
            quads.add(frontFace(state, materialInfo, depthOffset));
            quads.add(backFace(state, materialInfo, depthOffset));
        }
        return quads;
    }

    private static BakedQuad frontFace(ModelState state, BakedQuad.MaterialInfo materialInfo, float depthOffset) {
        TextureAtlasSprite sprite = materialInfo.sprite();
        return new BakedQuad(
                transform(state, 0.0F, 0.0F, 8.5F + depthOffset),
                transform(state, 16.0F, 0.0F, 8.5F + depthOffset),
                transform(state, 16.0F, 16.0F, 8.5F + depthOffset),
                transform(state, 0.0F, 16.0F, 8.5F + depthOffset),
                uv(sprite, 0.0F, 16.0F),
                uv(sprite, 16.0F, 16.0F),
                uv(sprite, 16.0F, 0.0F),
                uv(sprite, 0.0F, 0.0F),
                Direction.SOUTH,
                materialInfo
        );
    }

    private static BakedQuad backFace(ModelState state, BakedQuad.MaterialInfo materialInfo, float depthOffset) {
        TextureAtlasSprite sprite = materialInfo.sprite();
        return new BakedQuad(
                transform(state, 16.0F, 0.0F, 7.5F - depthOffset),
                transform(state, 0.0F, 0.0F, 7.5F - depthOffset),
                transform(state, 0.0F, 16.0F, 7.5F - depthOffset),
                transform(state, 16.0F, 16.0F, 7.5F - depthOffset),
                uv(sprite, 16.0F, 16.0F),
                uv(sprite, 0.0F, 16.0F),
                uv(sprite, 0.0F, 0.0F),
                uv(sprite, 16.0F, 0.0F),
                Direction.NORTH,
                materialInfo
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
        // TextureAtlasSprite#getU/getV use a normalized 0..1 offset in 26.1.2.
        // Passing 16 directly walks beyond this sprite and samples unrelated
        // entries from the atlas, which makes an item mask appear on other
        // fragments of the generated item plane.
        return UVPair.pack(sprite.getU(u * MODEL_UNIT), sprite.getV(v * MODEL_UNIT));
    }

}
