package committee.nova.mods.avaritia.init.compat.kubejs.event;

import committee.nova.mods.avaritia.core.singularity.SingularityReloadListener;
import committee.nova.mods.avaritia.core.singularity.SingularityValidationException;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingularityRegisterEventJSTest {
    private static final ResourceLocation INVALID_VALUE_ID = ResourceLocation.parse("test:kubejs_invalid_value");
    private static final ResourceLocation WRAPPED_INVALID_ID = ResourceLocation.parse("test:kubejs_wrapped_invalid");
    private static final ResourceLocation VALID_ID = ResourceLocation.parse("test:kubejs_valid");

    @Test
    void invalidIdsAndValuesOnlySkipTheirOwnDefinitions() {
        SingularityReloadListener listener = SingularityReloadListener.INSTANCE;
        listener.beginReload(Map.of());
        SingularityRegisterEventJS event = new SingularityRegisterEventJS();

        assertDoesNotThrow(() -> event.register("INVALID ID"));
        assertDoesNotThrow(() -> event.remove("INVALID ID"));
        assertDoesNotThrow(() -> event.removeRecipe("INVALID ID"));
        assertDoesNotThrow(() -> event.register(INVALID_VALUE_ID,
                singularity -> singularity.setTimeCost(0)));
        assertDoesNotThrow(() -> event.register(WRAPPED_INVALID_ID, singularity -> {
            throw new RuntimeException(new SingularityValidationException("wrapped validation failure"));
        }));
        assertDoesNotThrow(() -> event.register(VALID_ID));

        assertFalse(listener.getRunSingularities().containsKey(INVALID_VALUE_ID));
        assertFalse(listener.getRunSingularities().containsKey(WRAPPED_INVALID_ID));
        assertTrue(listener.getRunSingularities().containsKey(VALID_ID));
        listener.beginReload(Map.of());
    }

    @Test
    void unrelatedHandlerFailuresContinueToPropagate() {
        SingularityRegisterEventJS event = new SingularityRegisterEventJS();
        assertThrows(IllegalStateException.class, () -> event.register(VALID_ID, singularity -> {
            throw new IllegalStateException("unrelated handler failure");
        }));
    }
}
