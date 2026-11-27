package committee.nova.mods.avaritia.client.shader;

import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Avaritia shader pipelines")
class AvaritiaShadersTest {

    @Test
    @DisplayName("item effect overlay tests depth without writing it")
    void itemEffectOverlayDoesNotWriteDepth() {
        assertEquals(CompareOp.LESS_THAN_OR_EQUAL, AvaritiaShaders.ITEM_EFFECT_OVERLAY_DEPTH.depthTest());
        assertFalse(AvaritiaShaders.ITEM_EFFECT_OVERLAY_DEPTH.writeDepth());
    }

    @Test
    @DisplayName("item overlays stay transparent while armor keeps a cosmic interior")
    void cosmicSubstrateDependsOnEffect() {
        assertEquals(0.0F, AvaritiaShaderUniforms.Effect.COSMIC.substrateAlpha());
        assertEquals(1.0F, AvaritiaShaderUniforms.Effect.COSMIC_ARMOR.substrateAlpha());
    }

    @Test
    @DisplayName("armor glow samples texture transparency")
    void armorGlowUsesTexturedTranslucentPipeline() {
        assertSame(RenderPipelines.ARMOR_TRANSLUCENT,
                AvaritiaRenderTypeHelper.ARMOR_GLOW_PIPELINE);
    }
}
