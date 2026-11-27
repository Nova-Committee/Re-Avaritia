package committee.nova.mods.avaritia.api.client.screen.component;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.network.NetworkHooks;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockedStatic;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class PortableItemGridTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        try (MockedStatic<NetworkHooks> ignored = mockStatic(NetworkHooks.class)) {
            Bootstrap.bootStrap();
        }
    }

    @Test
    void selectionRequiresSameLiveCellAndOwnsItsStack() {
        List<ItemStack> choices = new ArrayList<>();
        PortableItemGrid grid = new PortableItemGrid(null, 10, 20, 2, 1, "test", choices::add);
        ItemStack named = new ItemStack(Items.DIRT, 8);
        named.setHoverName(Component.literal("variant"));
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
        ItemStack selected = choices.get(0);
        assertTrue(selected.is(Items.DIRT));
        assertEquals(Component.literal("variant"), selected.getHoverName());
        assertEquals(1, selected.getCount());
        selected.shrink(1);
        assertEquals(8, named.getCount(), "selection must not mutate the source inventory stack");
    }
}
