package committee.nova.mods.avaritia.client.model.loader;

import committee.nova.mods.avaritia.api.client.model.ItemQuadBakery;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Avaritia item model renderers")
class AvaritiaItemModelRenderersTest {

    @Test
    @DisplayName("uses each pulse quad's item-atlas render type")
    void pulseUsesTheBakedQuadItemRenderType() {
        NativeImage image = new NativeImage(16, 16, false);
        SpriteContents contents = new SpriteContents(
                Identifier.withDefaultNamespace("pulse_item_atlas_test"),
                new FrameSize(16, 16),
                image
        );

        try (TextureAtlasSprite sprite = new TestSprite(contents)) {
            List<BakedQuad> quads = ItemQuadBakery.bakeItem(sprite);
            RenderType itemAtlasRenderType = quads.getFirst().materialInfo().itemRenderType();

            AvaritiaItemModelRenderers.PulseLayerArgument argument =
                    new AvaritiaItemModelRenderers.PulseLayerArgument(quads);
            Map<RenderType, List<BakedQuad>> grouped = argument.quadsByRenderType();

            assertEquals(1, grouped.size());
            assertSame(quads.getFirst(), grouped.get(itemAtlasRenderType).getFirst());
        }
    }

    @Test
    @DisplayName("deduplicates item and block atlas render types for the effect barrier")
    void effectBarrierDeduplicatesBaseRenderTypes() {
        assertEffectBarrierRenderType(TextureAtlas.LOCATION_ITEMS, "effect_barrier_item_atlas_test");
        assertEffectBarrierRenderType(TextureAtlas.LOCATION_BLOCKS, "effect_barrier_block_atlas_test");
    }

    private static void assertEffectBarrierRenderType(Identifier atlas, String name) {
        NativeImage image = new NativeImage(16, 16, false);
        SpriteContents contents = new SpriteContents(
                Identifier.withDefaultNamespace(name),
                new FrameSize(16, 16),
                image
        );

        try (TextureAtlasSprite sprite = new TestSprite(atlas, contents)) {
            BakedQuad quad = ItemQuadBakery.bakeItem(sprite).getFirst();
            List<RenderType> renderTypes = AvaritiaItemModelRenderers.itemRenderTypes(List.of(quad, quad));

            assertEquals(1, renderTypes.size());
            assertSame(quad.materialInfo().itemRenderType(), renderTypes.getFirst());
        }
    }

    private static final class TestSprite extends TextureAtlasSprite {
        private TestSprite(SpriteContents contents) {
            this(TextureAtlas.LOCATION_ITEMS, contents);
        }

        private TestSprite(Identifier atlas, SpriteContents contents) {
            super(atlas, contents, 256, 256, 32, 64, 0);
        }
    }
}
