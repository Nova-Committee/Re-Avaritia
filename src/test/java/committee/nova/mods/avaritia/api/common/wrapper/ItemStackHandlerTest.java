package committee.nova.mods.avaritia.api.common.wrapper;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("26.3 默认数据组件绑定需要完整 tag 数据，单元测试环境暂不支持")
class ItemStackHandlerTest {
    private static HolderLookup.Provider registries;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        // 26.3 的默认数据组件不再随 Bootstrap 绑定，需手动应用后才能创建 ItemStack
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registries)
                .forEach(DataComponentInitializers.PendingComponents::apply);
    }

    @Test
    void simulationPreservesInventoryAndExecutionReturnsExactRemainder() {
        ItemStackHandler inventory = new ItemStackHandler(1);
        inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 60));
        ItemStack input = new ItemStack(Items.DIAMOND, 12);
        assertEquals(8, inventory.insertItem(0, input, true).getCount());
        assertEquals(60, inventory.getStackInSlot(0).getCount());
        assertEquals(12, input.getCount());
        assertEquals(8, inventory.insertItem(0, input, false).getCount());
        assertEquals(64, inventory.getStackInSlot(0).getCount());
        assertEquals(12, input.getCount());
        assertEquals(17, inventory.extractItem(0, 17, true).getCount());
        assertEquals(64, inventory.getStackInSlot(0).getCount());
        assertEquals(17, inventory.extractItem(0, 17, false).getCount());
        assertEquals(47, inventory.getStackInSlot(0).getCount());
        assertTrue(inventory.extractItem(0, 0, false).isEmpty());
        assertEquals(47, inventory.getStackInSlot(0).getCount());
    }

    @Test
    void distinctNbtDoesNotMergeAndSavedSlotsKeepExactStacks() {
        ItemStackHandler inventory = new ItemStackHandler(2);
        ItemStack named = new ItemStack(Items.DIAMOND, 7);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("stored variant"));
        inventory.setStackInSlot(1, named);
        ItemStack plain = new ItemStack(Items.DIAMOND, 3);
        assertEquals(3, inventory.insertItem(1, plain, false).getCount());
        assertEquals(7, inventory.getStackInSlot(1).getCount());
        ItemStackHandler restored = new ItemStackHandler(1);
        restored.deserializeNBT(registries, inventory.serializeNBT(registries));
        assertEquals(2, restored.getSlots());
        assertTrue(restored.getStackInSlot(0).isEmpty());
        assertEquals(7, restored.getStackInSlot(1).getCount());
        assertEquals("stored variant", restored.getStackInSlot(1).getHoverName().getString());
        restored.extractItem(1, 7, false);
        assertTrue(restored.getStackInSlot(1).isEmpty());
        assertEquals(7, inventory.getStackInSlot(1).getCount());
    }
}
