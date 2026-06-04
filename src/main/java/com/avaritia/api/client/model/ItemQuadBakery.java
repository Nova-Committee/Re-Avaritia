package com.avaritia.api.client.model;

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

    public static List<BakedQuad> bakeItem(TextureAtlasSprite... sprites) {
        return bakeItem(IDENTITY, sprites);
    }

    public static List<BakedQuad> bakeItem(RenderType renderType, TextureAtlasSprite... sprites) {
        return bakeItem(IDENTITY, renderType, sprites);
    }

    public static List<BakedQuad> bakeItem(ModelState state, TextureAtlasSprite... sprites) {
        return bakeItem(state, null, sprites);
    }

    public static List<BakedQuad> bakeItem(ModelState state, RenderType renderType, TextureAtlasSprite... sprites) {
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
            quads.add(frontFace(state, materialInfo));
            quads.add(backFace(state, materialInfo));
        }
        return quads;
    }

    private static BakedQuad frontFace(ModelState state, BakedQuad.MaterialInfo materialInfo) {
        TextureAtlasSprite sprite = materialInfo.sprite();
        return new BakedQuad(
                transform(state, 0.0F, 0.0F, 8.5F),
                transform(state, 16.0F, 0.0F, 8.5F),
                transform(state, 16.0F, 16.0F, 8.5F),
                transform(state, 0.0F, 16.0F, 8.5F),
                uv(sprite, 0.0F, 16.0F),
                uv(sprite, 16.0F, 16.0F),
                uv(sprite, 16.0F, 0.0F),
                uv(sprite, 0.0F, 0.0F),
                Direction.SOUTH,
                materialInfo
        );
    }

    private static BakedQuad backFace(ModelState state, BakedQuad.MaterialInfo materialInfo) {
        TextureAtlasSprite sprite = materialInfo.sprite();
        return new BakedQuad(
                transform(state, 16.0F, 0.0F, 7.5F),
                transform(state, 0.0F, 0.0F, 7.5F),
                transform(state, 0.0F, 16.0F, 7.5F),
                transform(state, 16.0F, 16.0F, 7.5F),
                uv(sprite, 16.0F, 16.0F),
                uv(sprite, 0.0F, 16.0F),
                uv(sprite, 0.0F, 0.0F),
                uv(sprite, 16.0F, 0.0F),
                Direction.NORTH,
                materialInfo
        );
    }

    private static Vector3fc transform(ModelState state, float x, float y, float z) {
        Matrix4fc matrix = state.transformation().getMatrix();
        return matrix.transformPosition(new Vector3f(x, y, z));
    }

    private static long uv(TextureAtlasSprite sprite, float u, float v) {
        return UVPair.pack(sprite.getU(u), sprite.getV(v));
    }

}
