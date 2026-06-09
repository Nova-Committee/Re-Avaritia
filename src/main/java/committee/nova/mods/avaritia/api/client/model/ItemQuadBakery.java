package committee.nova.mods.avaritia.api.client.model;

import com.mojang.math.Quadrant;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
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
    private static final CuboidFace.UVs SOUTH_FACE_UVS = new CuboidFace.UVs(0.0F, 0.0F, 16.0F, 16.0F);
    private static final CuboidFace.UVs NORTH_FACE_UVS = new CuboidFace.UVs(16.0F, 0.0F, 0.0F, 16.0F);

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

    public static List<BakedQuad> bakeGeneratedItem(ModelBaker baker, RenderType renderType, TextureAtlasSprite... sprites) {
        return bakeGeneratedItem(baker.interner(), IDENTITY, renderType, sprites);
    }

    public static List<BakedQuad> bakeGeneratedItem(ModelBaker.Interner interner, ModelState state, RenderType renderType, TextureAtlasSprite... sprites) {
        List<BakedQuad> quads = new LinkedList<>();
        for (int i = 0; i < sprites.length; i++) {
            BakedQuad.MaterialInfo materialInfo = new BakedQuad.MaterialInfo(
                    sprites[i],
                    ChunkSectionLayer.TRANSLUCENT,
                    renderType != null ? renderType : RenderTypes.itemTranslucent(sprites[i].atlasLocation()),
                    i,
                    true,
                    0
            );
            QuadCollection.Builder builder = new QuadCollection.Builder();
            bakeGeneratedSprite(builder, interner, state, materialInfo);
            quads.addAll(builder.build().getAll());
        }
        return quads;
    }

    private static void bakeGeneratedSprite(QuadCollection.Builder builder, ModelBaker.Interner interner, ModelState state,
                                            BakedQuad.MaterialInfo materialInfo) {
        Vector3f from = new Vector3f(0.0F, 0.0F, 7.5F);
        Vector3f to = new Vector3f(16.0F, 16.0F, 8.5F);
        builder.addUnculledFace(FaceBakery.bakeQuad(
                interner, from, to, SOUTH_FACE_UVS, Quadrant.R0, materialInfo, Direction.SOUTH, state, null
        ));
        builder.addUnculledFace(FaceBakery.bakeQuad(
                interner, from, to, NORTH_FACE_UVS, Quadrant.R0, materialInfo, Direction.NORTH, state, null
        ));
        ItemModelGenerator.bakeSideFaces(builder, interner, state, materialInfo);
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
