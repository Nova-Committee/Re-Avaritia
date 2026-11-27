package committee.nova.mods.avaritia.common.menu;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.network.NetworkHooks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mockStatic;

class ExtremeAnvilMenuTest {
    private MockedStatic<ForgeHooks> forgeHooks;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        // Raw JUnit runs do not include ModLauncher's Forge event bytecode transformations.
        try (MockedStatic<NetworkHooks> ignored = mockStatic(NetworkHooks.class)) {
            Bootstrap.bootStrap();
        }
    }

    @BeforeEach
    void mockForgeAnvilHook() {
        this.forgeHooks = mockStatic(ForgeHooks.class);
        this.forgeHooks.when(() -> ForgeHooks.onAnvilChange(
                any(AnvilMenu.class),
                any(ItemStack.class),
                any(ItemStack.class),
                any(Container.class),
                nullable(String.class),
                anyInt(),
                nullable(Player.class))).thenReturn(true);
    }

    @AfterEach
    void closeForgeAnvilHook() {
        this.forgeHooks.close();
    }

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
    void honorsForgeAnvilRecipeOverrides() {
        this.forgeHooks.when(() -> ForgeHooks.onAnvilChange(
                any(AnvilMenu.class),
                any(ItemStack.class),
                any(ItemStack.class),
                any(Container.class),
                nullable(String.class),
                anyInt(),
                nullable(Player.class))).thenAnswer(invocation -> {
                    ItemStack left = invocation.getArgument(1);
                    ItemStack right = invocation.getArgument(2);
                    if (!left.is(Items.STICK) || !right.is(Items.DIAMOND)) {
                        return true;
                    }

                    AnvilMenu menu = invocation.getArgument(0);
                    Container output = invocation.getArgument(3);
                    output.setItem(0, new ItemStack(Items.NETHER_STAR));
                    menu.setMaximumCost(7);
                    menu.repairItemCountCost = 1;
                    return false;
                });

        ExtremeAnvilMenu menu = createMenu();
        menu.getSlot(0).set(new ItemStack(Items.STICK));
        menu.getSlot(1).set(new ItemStack(Items.DIAMOND));

        assertTrue(menu.getSlot(2).getItem().is(Items.NETHER_STAR));
        assertEquals(7, menu.getCost());
        assertEquals(1, menu.repairItemCountCost);
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
