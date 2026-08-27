package committee.nova.mods.avaritia.client.shader;

import com.mojang.blaze3d.platform.CompareOp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Avaritia shader pipelines")
class AvaritiaShadersTest {

    @Test
    @DisplayName("item effect overlay tests depth without writing it")
    void itemEffectOverlayDoesNotWriteDepth() {
        assertEquals(CompareOp.LESS_THAN_OR_EQUAL, AvaritiaShaders.ITEM_EFFECT_OVERLAY_DEPTH.depthTest());
        assertFalse(AvaritiaShaders.ITEM_EFFECT_OVERLAY_DEPTH.writeDepth());
    }
}
