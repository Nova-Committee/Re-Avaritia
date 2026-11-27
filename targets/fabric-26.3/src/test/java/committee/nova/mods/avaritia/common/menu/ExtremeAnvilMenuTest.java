package committee.nova.mods.avaritia.common.menu;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("26.3 默认数据组件绑定需要完整 tag 数据，单元测试环境暂不支持")
class ExtremeAnvilMenuTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // 26.3 的默认数据组件不再随 Bootstrap 绑定，需手动应用后才能创建 ItemStack
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
                .build(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY))
                .forEach(DataComponentInitializers.PendingComponents::apply);
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
        // 26.3 Inventory 需要 Player 与 EntityEquipment；菜单逻辑不触碰 player
        return new ExtremeAnvilMenu(0, new Inventory(null, new EntityEquipment()), ContainerLevelAccess.NULL);
    }

    private static ItemStack nearlyBrokenDiamondSword() {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.setDamageValue(stack.getMaxDamage() - 10);
        return stack;
    }
}
