package committee.nova.mods.avaritia.api.client.screen.component;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortableLayoutTest {
    @Test
    void fractionalCursorUsesHalfOpenBounds() {
        ScreenRectangle bounds = new ScreenRectangle(0, 20, 30, 40);
        assertFalse(PortableLayout.contains(bounds, -0.25, 21));
        assertTrue(PortableLayout.contains(bounds, 0, 21));
        assertFalse(PortableLayout.contains(bounds, 30, 21));
        assertFalse(PortableLayout.contains(new ScreenRectangle(0, 20, 0, 40), 0, 21));
        assertFalse(PortableLayout.contains(new ScreenRectangle(0, 20, 30, 0), 1, 20));
    }
}
