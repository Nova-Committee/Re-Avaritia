package committee.nova.mods.avaritia.common.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtremeAnvilMenuTest {

    @Test
    void usesVanillaAnvilMenuContract() {
        assertTrue(AnvilMenu.class.isAssignableFrom(ExtremeAnvilMenu.class));
    }

    @Test
    void combinesVanillaDamageableItemsWithoutExperienceRequirement() {
        ExtremeAnvilMenu menu = createMenu();
        ItemStack left = nearlyBrokenDiamondSword();
        ItemStack right = nearlyBrokenDiamondSword();

        menu.getSlot(0).set(left);
        menu.getSlot(1).set(right);

        ItemStack result = menu.getSlot(2).getItem();
        assertFalse(result.isEmpty());
        assertTrue(result.getDamageValue() < left.getDamageValue());
        assertTrue(menu.mayPickup(null, true));
    }

    @Test
    void supportsVanillaRenameOperation() {
        ExtremeAnvilMenu menu = createMenu();
        menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));

        assertTrue(menu.setItemName("Extreme Blade"));

        assertEquals("Extreme Blade", menu.getSlot(2).getItem().getHoverName().getString());
    }

    @Test
    void honorsNeoForgeAnvilRecipeOverrides() {
        Consumer<AnvilUpdateEvent> listener = event -> {
            if (event.getLeft().is(Items.STICK) && event.getRight().is(Items.DIAMOND)) {
                event.setOutput(new ItemStack(Items.NETHER_STAR));
                event.setCost(7);
                event.setMaterialCost(1);
            }
        };
        NeoForge.EVENT_BUS.addListener(listener);

        try {
            ExtremeAnvilMenu menu = createMenu();
            menu.getSlot(0).set(new ItemStack(Items.STICK));
            menu.getSlot(1).set(new ItemStack(Items.DIAMOND));

            assertTrue(menu.getSlot(2).getItem().is(Items.NETHER_STAR));
            assertEquals(7, menu.getCost());
            assertEquals(1, menu.repairItemCountCost);
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
    }

    private static ExtremeAnvilMenu createMenu() {
        return new ExtremeAnvilMenu(0, new Inventory(null), ContainerLevelAccess.NULL);
    }

    private static ItemStack nearlyBrokenDiamondSword() {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.setDamageValue(stack.getMaxDamage() - 10);
        return stack;
    }
}
