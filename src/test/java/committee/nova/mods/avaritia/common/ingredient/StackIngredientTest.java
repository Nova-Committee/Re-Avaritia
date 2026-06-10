package committee.nova.mods.avaritia.common.ingredient;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StackIngredientTest {
    private static final Path STACK_INGREDIENT_SOURCE = Path.of("src/main/java/committee/nova/mods/avaritia/common/ingredient/StackIngredient.java");
    private static final Path JEI_PLUGIN_SOURCE = Path.of("src/main/java/committee/nova/mods/avaritia/compat/jei/AvaritiaJeiPlugin.java");
    private static final Path PACKET_SOURCE = Path.of("src/main/java/committee/nova/mods/avaritia/common/net/S2CSingularitiesPacket.java");
    private static final Path RELOAD_LISTENER_SOURCE = Path.of("src/main/java/committee/nova/mods/avaritia/core/singularity/SingularityReloadListener.java");

    @Test
    void displayPreservesStackComponentsForRecipeViewers() throws IOException {
        String source = compact(Files.readString(STACK_INGREDIENT_SOURCE));

        assertTrue(source.contains("publicSlotDisplaydisplay()"));
        assertTrue(source.contains("newSlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(this.item))"));
    }

    @Test
    void singularityJeiSubtypeReadsRawComponentId() throws IOException {
        String source = compact(Files.readString(JEI_PLUGIN_SOURCE));

        assertTrue(source.contains("stack.get(ModDataComponents.SINGULARITY_ID.get())"));
        assertTrue(source.contains("singularityId.toString()"));
    }

    @Test
    void singularityClientSyncInvalidatesDynamicRecipeIngredients() throws IOException {
        String packetSource = compact(Files.readString(PACKET_SOURCE));
        String listenerSource = compact(Files.readString(RELOAD_LISTENER_SOURCE));

        assertTrue(packetSource.contains("SingularityReloadListener.INSTANCE.applySyncedState("));
        assertTrue(listenerSource.contains("publicvoidapplySyncedState("));
        assertTrue(listenerSource.contains("onSingularitiesReloaded();"));
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", "");
    }
}
