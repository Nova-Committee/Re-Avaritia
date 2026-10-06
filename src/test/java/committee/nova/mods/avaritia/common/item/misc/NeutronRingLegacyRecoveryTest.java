package committee.nova.mods.avaritia.common.item.misc;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


class NeutronRingLegacyRecoveryTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
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

}
