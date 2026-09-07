package committee.nova.mods.avaritia.api.client.screen.component;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortableItemGridTest {
    @Test
    void selectionRequiresSameLiveCellAndOwnsItsStack() {
        List<ItemStack> choices = new ArrayList<>();
        PortableItemGrid grid = new PortableItemGrid(null, 10, 20, 2, 1, "test", choices::add);
        ItemStack named = new ItemStack(Items.DIRT, 8);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("variant"));
        grid.setItems(List.of(named, new ItemStack(Items.STONE), new ItemStack(Items.DIAMOND)));
        grid.onClick(12.25, 22.25);
        grid.onRelease(31.25, 22.25);
        assertTrue(choices.isEmpty(), "release in a different cell must not select");
        grid.onClick(12.25, 22.25);
        grid.setScrollOffset(1);
        grid.onRelease(12.25, 22.25);
        assertTrue(choices.isEmpty(), "scroll remapping must cancel an old press");
        grid.setScrollOffset(0);
        grid.onClick(30, 22);
        grid.onRelease(30, 22);
        assertTrue(choices.isEmpty(), "cell gap must not select");
        grid.onClick(12.25, 22.25);
        grid.cancelInteraction();
        grid.onRelease(12.25, 22.25);
        assertTrue(choices.isEmpty(), "a release outside the grid must terminate the gesture");
        grid.onClick(12.25, 22.25);
        grid.onRelease(12.25, 22.25);
        assertEquals(1, choices.size());
        ItemStack selected = choices.getFirst();
        assertTrue(selected.is(Items.DIRT));
        assertEquals(Component.literal("variant"), selected.get(DataComponents.CUSTOM_NAME));
        assertEquals(1, selected.getCount());
        selected.shrink(1);
        assertEquals(8, named.getCount(), "selection must not mutate the source inventory stack");
    }
}
