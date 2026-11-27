package committee.nova.mods.avaritia.client.shader;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Avaritia shader pipelines")
class AvaritiaShadersTest {

    @Test
    @Disabled("1.21.11: the depth state lives on registered RenderPipelines, which cannot be built in a headless unit test")
    @DisplayName("effect pipelines test depth without writing it")
    void effectPipelinesDoNotWriteDepth() {
        List<RenderPipeline> registered = new ArrayList<>();
        AvaritiaShaders.onRegisterShaders(new RegisterRenderPipelinesEvent(registered::add));

        assertFalse(registered.isEmpty());
        for (RenderPipeline pipeline : registered) {
            assertEquals(DepthTestFunction.LEQUAL_DEPTH_TEST, pipeline.getDepthTestFunction());
            assertFalse(pipeline.isWriteDepth());
        }
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
