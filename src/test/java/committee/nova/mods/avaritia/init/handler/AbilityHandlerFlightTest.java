package committee.nova.mods.avaritia.init.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AbilityHandlerFlightTest {
    @Test
    void unchangedFlightSourcesDoNotDirtyTheAttribute() {
        AtomicInteger updates = new AtomicInteger();
        AttributeInstance flight = new AttributeInstance(NeoForgeMod.CREATIVE_FLIGHT, ignored -> updates.incrementAndGet());

        AbilityHandler.updateFlightModifier(flight, false, false);
        assertEquals(0, updates.get());
        AbilityHandler.updateFlightModifier(flight, true, false);
        assertEquals(1.0D, flight.getValue());
        assertEquals(1, updates.get());

        for (int tick = 0; tick < 100; tick++) {
            AbilityHandler.updateFlightModifier(flight, true, false);
            AbilityHandler.updateFlightModifier(flight, true, true);
            AbilityHandler.updateFlightModifier(flight, false, true);
        }
        assertEquals(1, updates.get(), "Retaining either source must not trigger attribute sync");

        AbilityHandler.updateFlightModifier(flight, false, false);
        assertEquals(0.0D, flight.getValue());
        assertEquals(2, updates.get());
        AbilityHandler.updateFlightModifier(flight, false, false);
        assertEquals(2, updates.get());
        AbilityHandler.updateFlightModifier(flight, false, true);
        assertEquals(1.0D, flight.getValue());
        assertEquals(3, updates.get());
    }

    @Test
    void removingOurFlightPreservesOtherModsModifiers() {
        AttributeInstance flight = new AttributeInstance(NeoForgeMod.CREATIVE_FLIGHT, ignored -> {});
        ResourceLocation other = ResourceLocation.fromNamespaceAndPath("othermod", "flight");
        flight.addTransientModifier(new AttributeModifier(other, 1.0D, AttributeModifier.Operation.ADD_VALUE));
        AbilityHandler.updateFlightModifier(flight, true, false);
        assertEquals(1.0D, flight.getValue());
        AbilityHandler.updateFlightModifier(flight, false, false);
        assertTrue(flight.hasModifier(other));
        assertEquals(1.0D, flight.getValue());
    }

    @Test
    void lostModifierIsRestoredWhileASourceRemains() {
        AttributeInstance flight = new AttributeInstance(NeoForgeMod.CREATIVE_FLIGHT, ignored -> {});
        AbilityHandler.updateFlightModifier(flight, false, true);
        flight.removeModifier(ResourceLocation.fromNamespaceAndPath("avaritia", "infinity_chest_flight"));
        assertEquals(0.0D, flight.getValue());
        AbilityHandler.updateFlightModifier(flight, false, true);
        assertEquals(1.0D, flight.getValue());
    }

    @Test
    void missingFlightAttributeIsSafe() {
        assertDoesNotThrow(() -> AbilityHandler.updateFlightModifier(null, true, true));
    }
}
