package committee.nova.mods.avaritia.common.item.misc;

import committee.nova.mods.avaritia.core.io.SideConfiguration;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static committee.nova.mods.avaritia.common.item.misc.SideConfigurationCardItem.CardAction.APPLY;
import static committee.nova.mods.avaritia.common.item.misc.SideConfigurationCardItem.CardAction.CLEAR;
import static committee.nova.mods.avaritia.common.item.misc.SideConfigurationCardItem.CardAction.PASS;
import static committee.nova.mods.avaritia.common.item.misc.SideConfigurationCardItem.CardAction.SAVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SideConfigurationCardItemTest {

    @Test
    void shiftOnCompatibleTileAlwaysSavesOrOverwrites() {
        assertEquals(SAVE, SideConfigurationCardItem.decideAction(true, true, false));
        assertEquals(SAVE, SideConfigurationCardItem.decideAction(true, true, true));
    }

    @Test
    void normalUseOnCompatibleTileAppliesSavedConfiguration() {
        assertEquals(APPLY, SideConfigurationCardItem.decideAction(true, false, true));
    }

    @Test
    void normalUseOnCompatibleTilePassesWhenCardIsEmpty() {
        assertEquals(PASS, SideConfigurationCardItem.decideAction(true, false, false));
    }

    @Test
    void shiftOnIncompatibleBlockClearsSavedConfiguration() {
        assertEquals(CLEAR, SideConfigurationCardItem.decideAction(false, true, true));
    }

    @Test
    void interactionsWithoutAnExecutableActionPass() {
        assertEquals(PASS, SideConfigurationCardItem.decideAction(false, true, false));
        assertEquals(PASS, SideConfigurationCardItem.decideAction(false, false, false));
        assertEquals(PASS, SideConfigurationCardItem.decideAction(false, false, true));
    }

    @Test
    void savedConfigurationRoundTripsAndClearsThroughItemNbt() {
        ItemStack stack = new ItemStack(Items.PAPER);
        SideConfiguration original = new SideConfiguration();
        original.setSideMode(Direction.NORTH, SideConfiguration.SideMode.ACTIVE_INPUT);
        original.setSideMode(Direction.SOUTH, SideConfiguration.SideMode.PASSIVE_OUTPUT);
        original.setSideMode(Direction.UP, SideConfiguration.SideMode.ACTIVE_MIXIN);

        assertFalse(SideConfigurationCardItem.hasSavedConfig(stack));

        SideConfigurationCardItem.saveConfigToItem(stack, original);

        assertTrue(SideConfigurationCardItem.hasSavedConfig(stack));
        assertConfigurationEquals(original, SideConfigurationCardItem.loadConfigFromItem(stack));

        SideConfigurationCardItem.clearSavedConfig(stack);

        assertFalse(SideConfigurationCardItem.hasSavedConfig(stack));
    }

    private static void assertConfigurationEquals(SideConfiguration expected, SideConfiguration actual) {
        for (Direction direction : Direction.values()) {
            assertEquals(expected.getSideMode(direction), actual.getSideMode(direction), direction.getName());
        }
    }
}
