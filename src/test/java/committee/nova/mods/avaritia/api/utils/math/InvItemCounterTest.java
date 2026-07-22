package committee.nova.mods.avaritia.api.utils.math;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvItemCounterTest {

    @Test
    void countsMatchingComponentStacksByInventoryAmount() {
        Inventory inventory = new Inventory(null);
        CompoundTag data = new CompoundTag();
        data.putString("variant", "test");

        ItemStack first = new ItemStack(Items.STONE, 3);
        first.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        ItemStack second = new ItemStack(Items.STONE, 4);
        second.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        inventory.items.set(0, first);
        inventory.items.set(10, second);

        ItemStack query = new ItemStack(Items.STONE);
        query.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        assertEquals(7, new InvItemCounter(inventory).getCount(query));
    }
}
