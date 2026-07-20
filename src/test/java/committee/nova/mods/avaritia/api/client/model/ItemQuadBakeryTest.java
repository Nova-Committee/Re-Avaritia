package committee.nova.mods.avaritia.api.client.model;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import org.joml.Vector3fc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Item quad bakery")
class ItemQuadBakeryTest {
    private static final float EPSILON = 0.0001F;

    @Test
    @DisplayName("normalizes 0..16 model coordinates before creating baked quad vertices")
    void transformNormalizesModelCoordinates() {
        Vector3fc origin = ItemQuadBakery.transform(ItemQuadBakery.IDENTITY, 0.0F, 0.0F, 7.5F);
        Vector3fc oppositeCorner = ItemQuadBakery.transform(ItemQuadBakery.IDENTITY, 16.0F, 16.0F, 8.5F);

        assertAll(
                () -> assertEquals(0.0F, origin.x(), EPSILON),
                () -> assertEquals(0.0F, origin.y(), EPSILON),
                () -> assertEquals(7.5F / 16.0F, origin.z(), EPSILON),
                () -> assertEquals(1.0F, oppositeCorner.x(), EPSILON),
                () -> assertEquals(1.0F, oppositeCorner.y(), EPSILON),
                () -> assertEquals(8.5F / 16.0F, oppositeCorner.z(), EPSILON)
        );
    }

    @Test
    @DisplayName("keeps direct item quad UVs inside the source atlas sprite")
    void directQuadUvsStayInsideSprite() {
        NativeImage image = new NativeImage(16, 16, false);
        SpriteContents contents = new SpriteContents(
                Identifier.withDefaultNamespace("item_quad_bakery_test"),
                new FrameSize(16, 16),
                image
        );

        try (TextureAtlasSprite sprite = new TestSprite(contents)) {
            List<BakedQuad> quads = ItemQuadBakery.bakeItem(sprite);

            assertEquals(2, quads.size());
            for (BakedQuad quad : quads) {
                for (int vertex = 0; vertex < BakedQuad.VERTEX_COUNT; vertex++) {
                    float u = UVPair.unpackU(quad.packedUV(vertex));
                    float v = UVPair.unpackV(quad.packedUV(vertex));
                    assertAll(
                            () -> assertTrue(u >= sprite.getU0() - EPSILON && u <= sprite.getU1() + EPSILON),
                            () -> assertTrue(v >= sprite.getV0() - EPSILON && v <= sprite.getV1() + EPSILON)
                    );
                }
            }
        }
    }

    @Test
    @DisplayName("insets overlay faces and offsets them outwards")
    void overlayFacesAreInsetAndOffsetOutwards() {
        NativeImage image = new NativeImage(16, 16, false);
        SpriteContents contents = new SpriteContents(
                Identifier.withDefaultNamespace("item_quad_overlay_test"),
                new FrameSize(16, 16),
                image
        );

        try (TextureAtlasSprite sprite = new TestSprite(contents)) {
            List<BakedQuad> base = ItemQuadBakery.bakeItem(sprite);
            List<BakedQuad> overlay = ItemQuadBakery.bakeItemOverlay(null, sprite);
            float inset = 0.25F / 16.0F;

            assertAll(
                    () -> assertEquals(2, overlay.size()),
                    () -> assertTrue(overlay.get(0).position(0).z() > base.get(0).position(0).z()),
                    () -> assertTrue(overlay.get(1).position(0).z() < base.get(1).position(0).z()),
                    () -> assertEquals(inset, overlay.get(0).position(0).x(), EPSILON),
                    () -> assertEquals(inset, overlay.get(0).position(0).y(), EPSILON),
                    () -> assertEquals(1.0F - inset, overlay.get(0).position(2).x(), EPSILON),
                    () -> assertEquals(1.0F - inset, overlay.get(0).position(2).y(), EPSILON),
                    () -> assertEquals(1.0F - inset, overlay.get(1).position(0).x(), EPSILON),
                    () -> assertEquals(inset, overlay.get(1).position(0).y(), EPSILON)
            );
        }
    }

    private static final class TestSprite extends TextureAtlasSprite {
        private TestSprite(SpriteContents contents) {
            super(TextureAtlas.LOCATION_BLOCKS, contents, 256, 256, 32, 64, 0);
        }
    }
}
