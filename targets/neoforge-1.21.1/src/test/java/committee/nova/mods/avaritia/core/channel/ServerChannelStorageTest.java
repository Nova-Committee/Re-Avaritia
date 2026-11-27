package committee.nova.mods.avaritia.core.channel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerChannelStorageTest {

    @Test
    void saturatesLongBackedStorageWithoutOverflow() {
        ServerChannel channel = new ServerChannel("test");

        assertEquals(Long.MAX_VALUE - 2, channel.addItem("minecraft:stone", Long.MAX_VALUE - 2));
        assertEquals(2, channel.addItem("minecraft:stone", 5));
        assertEquals(Long.MAX_VALUE, channel.getRealItemAmount("minecraft:stone"));

        assertEquals(Long.MAX_VALUE - 1, channel.addFluid("minecraft:water", Long.MAX_VALUE - 1));
        assertEquals(1, channel.addFluid("minecraft:water", 8));
        assertEquals(Long.MAX_VALUE, channel.getRealFluidAmount("minecraft:water"));

        assertEquals(Long.MAX_VALUE - 3, channel.addEnergy("test:energy", Long.MAX_VALUE - 3));
        assertEquals(3, channel.addEnergy("test:energy", 10));
        assertEquals(Long.MAX_VALUE, channel.getRealEnergyAmount("test:energy"));
    }

    @Test
    void rejectsNonPositiveAmountsAndRoundTripsLongNbt() {
        ServerChannel channel = new ServerChannel("x".repeat(80));
        assertEquals(0, channel.addItem("minecraft:stone", -1));
        assertEquals(0, channel.addFluid("minecraft:water", -1));
        assertEquals(0, channel.addEnergy("test:energy", -1));

        channel.addItem("minecraft:stone", (long) Integer.MAX_VALUE + 5);
        channel.addFluid("minecraft:water", (long) Integer.MAX_VALUE + 7);
        channel.addEnergy("test:energy", (long) Integer.MAX_VALUE + 9);

        ServerChannel restored = new ServerChannel(channel.buildData());
        assertEquals(64, restored.getName().length());
        assertEquals((long) Integer.MAX_VALUE + 5, restored.getRealItemAmount("minecraft:stone"));
        assertEquals((long) Integer.MAX_VALUE + 7, restored.getRealFluidAmount("minecraft:water"));
        assertEquals((long) Integer.MAX_VALUE + 9, restored.getRealEnergyAmount("test:energy"));
    }

    @Test
    void rejectsNegativeExtractionAmounts() {
        ServerChannel channel = new ServerChannel("test");
        channel.addItem("minecraft:stone", 8);
        channel.addFluid("minecraft:water", 9);
        channel.addEnergy(10);

        assertTrue(channel.takeItem("minecraft:stone", -1).isEmpty());
        assertTrue(channel.takeFluid("minecraft:water", -1).isEmpty());
        assertEquals(0, channel.extractEnergy(-1, false));
        channel.removeItem("minecraft:stone", -1);
        channel.removeEnergy(-1L);

        assertEquals(8, channel.getRealItemAmount("minecraft:stone"));
        assertEquals(9, channel.getRealFluidAmount("minecraft:water"));
        assertEquals(10, channel.getRealEnergyAmount("avaritia:forge_energy"));
    }

    @Test
    void reportsUnavailableRegistryEntriesAsIncomplete() {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        net.minecraft.nbt.CompoundTag items = new net.minecraft.nbt.CompoundTag();
        items.putLong("missing_mod:removed_item", 42L);
        data.put("items", items);

        ServerChannel channel = new ServerChannel(data);

        assertTrue(!channel.isLoadComplete());
        assertEquals(0, channel.getRealItemAmount("missing_mod:removed_item"));
    }
}
