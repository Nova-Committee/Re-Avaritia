package committee.nova.mods.avaritia.common.menu;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtremeAnvilMenuTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
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

    private static ExtremeAnvilMenu createMenu() {
        return new ExtremeAnvilMenu(0, new Inventory(null), ContainerLevelAccess.NULL);
    }

    private static ItemStack nearlyBrokenDiamondSword() {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.setDamageValue(stack.getMaxDamage() - 10);
        return stack;
    }
}
