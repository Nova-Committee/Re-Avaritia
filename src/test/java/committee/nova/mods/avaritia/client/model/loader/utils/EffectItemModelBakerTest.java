package committee.nova.mods.avaritia.client.model.loader.utils;

import committee.nova.mods.avaritia.api.client.model.ItemQuadBakery;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Effect item model baker")
class EffectItemModelBakerTest {

    @Test
    @DisplayName("reuses resolved model quads for the pulse layer")
    void pulseLayerReusesResolvedModelQuads() {
        NativeImage image = new NativeImage(16, 16, false);
        SpriteContents contents = new SpriteContents(
                Identifier.withDefaultNamespace("effect_item_model_baker_test"),
                new FrameSize(16, 16),
                image
        );

        try (TextureAtlasSprite sprite = new TestSprite(contents)) {
            BakedQuad originalQuad = ItemQuadBakery.bakeItem(sprite).getFirst();
            QuadCollection geometry = new QuadCollection.Builder().addUnculledFace(originalQuad).build();
            TextureSlots textureSlots = TextureSlots.EMPTY;
            ResolvedModel resolvedModel = new TestResolvedModel(geometry, textureSlots);

            List<BakedQuad> actual = EffectItemModelBaker.bakeBaseQuads(null, resolvedModel, textureSlots);

            assertEquals(1, actual.size());
            assertSame(originalQuad, actual.getFirst(), "pulse must retain the generated model geometry and frame-aware UVs");
        }
    }

    private record TestResolvedModel(QuadCollection geometry, TextureSlots expectedTextureSlots) implements ResolvedModel {
        @Override
        public UnbakedModel wrapped() {
            return new UnbakedModel() {
            };
        }

        @Override
        public ResolvedModel parent() {
            return null;
        }

        @Override
        public String debugName() {
            return "effect_item_model_baker_test";
        }

        @Override
        public QuadCollection bakeTopGeometry(TextureSlots textureSlots, ModelBaker baker, ModelState state) {
            assertSame(this.expectedTextureSlots, textureSlots);
            assertSame(BlockModelRotation.IDENTITY, state);
            return this.geometry;
        }
    }

    private static final class TestSprite extends TextureAtlasSprite {
        private TestSprite(SpriteContents contents) {
            super(TextureAtlas.LOCATION_BLOCKS, contents, 256, 256, 32, 64, 0);
        }
    }
}
