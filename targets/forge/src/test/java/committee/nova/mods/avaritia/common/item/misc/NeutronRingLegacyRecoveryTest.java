package committee.nova.mods.avaritia.common.item.misc;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class NeutronRingLegacyRecoveryTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        try (MockedStatic<NetworkHooks> ignored = mockStatic(NetworkHooks.class)) {
            Bootstrap.bootStrap();
        }
    }
    @Test
    void copySlotsDoesNotMutateHandler() {
        ItemStackHandler handler = new ItemStackHandler(81);
        ItemStack named = new ItemStack(Items.DIAMOND, 7);
        named.setHoverName(net.minecraft.network.chat.Component.literal("Keep me"));
        handler.setStackInSlot(3, named);
        handler.setStackInSlot(10, new ItemStack(Items.OAK_LOG, 64));

        List<ItemStack> copied = NeutronRingLegacyRecovery.copySlots(handler);
        assertEquals(2, copied.size());
        assertEquals(7, handler.getStackInSlot(3).getCount());
        assertEquals("Keep me", handler.getStackInSlot(3).getHoverName().getString());
        assertEquals(7, copied.get(0).getCount());
        assertEquals("Keep me", copied.get(0).getHoverName().getString());
        copied.get(0).shrink(7);
        assertEquals(7, handler.getStackInSlot(3).getCount());
    }

    @Test
    void escrowRoundTripPreservesExactRemainderNbt() {
        NeutronRingLegacyRecovery data = new NeutronRingLegacyRecovery();
        UUID player = UUID.randomUUID();
        ItemStack named = new ItemStack(Items.DIAMOND_SWORD);
        named.setHoverName(net.minecraft.network.chat.Component.literal("Heirloom"));
        named.setDamageValue(12);
        data.store(player, List.of(named, new ItemStack(Items.COBBLESTONE, 64)));
        CompoundTag saved = data.save(new CompoundTag());

        NeutronRingLegacyRecovery loaded = NeutronRingLegacyRecovery.load(saved);
        List<ItemStack> remainders = loaded.remaining(player);
        assertEquals(2, remainders.size());
        assertEquals(Items.DIAMOND_SWORD, remainders.get(0).getItem());
        assertEquals("Heirloom", remainders.get(0).getHoverName().getString());
        assertEquals(12, remainders.get(0).getDamageValue());
        assertEquals(64, remainders.get(1).getCount());
        assertEquals(Items.COBBLESTONE, remainders.get(1).getItem());
    }

    @Test
    void recoveredFlagIsWrittenInSavedPlayerEntry() {
        NeutronRingLegacyRecovery data = new NeutronRingLegacyRecovery();
        UUID player = UUID.randomUUID();
        data.store(player, List.of(new ItemStack(Items.DIRT)));
        CompoundTag saved = data.save(new CompoundTag());
        ListTag players = saved.getList("Players", 10);
        assertFalse(players.isEmpty());
        assertTrue(NbtUtils.loadUUID(players.getCompound(0).get("Id")).equals(player));
        assertEquals(1, players.getCompound(0).getList("Items", 10).size());
    }
}
