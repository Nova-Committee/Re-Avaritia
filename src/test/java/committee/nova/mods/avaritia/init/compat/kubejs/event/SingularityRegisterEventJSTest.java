package committee.nova.mods.avaritia.init.compat.kubejs.event;

import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingularityRegisterEventJSTest {
    private static final ResourceLocation INVALID_ID = ResourceLocation.parse("test:kubejs_invalid");
    private static final ResourceLocation VALID_ID = ResourceLocation.parse("test:kubejs_valid");

    @Test
    void invalidBuilderOnlySkipsItsOwnEntry() {
        SingularityReloadListener listener = SingularityReloadListener.INSTANCE;
        listener.beginReload(Map.of());
        SingularityRegisterEventJS event = new SingularityRegisterEventJS(null);

        assertDoesNotThrow(() -> event.register(INVALID_ID, singularity -> singularity.setTimeCost(0)));
        assertDoesNotThrow(() -> event.register(VALID_ID));

        assertFalse(listener.getRunSingularities().containsKey(INVALID_ID));
        assertTrue(listener.getRunSingularities().containsKey(VALID_ID));
        listener.beginReload(Map.of());
    }
}
