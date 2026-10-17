package committee.nova.mods.avaritia.common.item.misc;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Disabled("26.3 默认数据组件绑定需要完整 tag 数据，单元测试环境暂不支持")
class NeutronRingLegacyRecoveryTest {
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
    void escrowRoundTripPreservesExactRemainderNbt() {
        NeutronRingLegacyRecovery data = new NeutronRingLegacyRecovery();
        UUID player = UUID.randomUUID();
        ItemStack named = new ItemStack(Items.DIAMOND_SWORD);
        named.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Heirloom"));
        named.setDamageValue(12);
        data.store(player, List.of(named, new ItemStack(Items.COBBLESTONE, 64)));
        // 26.3 SavedData 持久化走 Codec；用 NbtOps 做等价往返
        CompoundTag saved = (CompoundTag) NeutronRingLegacyRecovery.CODEC
                .encodeStart(NbtOps.INSTANCE, data).getOrThrow();

        NeutronRingLegacyRecovery loaded = NeutronRingLegacyRecovery.CODEC
                .parse(NbtOps.INSTANCE, saved).getOrThrow();
        List<ItemStack> remainders = loaded.remaining(player);
        assertEquals(2, remainders.size());
        assertEquals(Items.DIAMOND_SWORD, remainders.get(0).getItem());
        assertEquals("Heirloom", remainders.get(0).getHoverName().getString());
        assertEquals(12, remainders.get(0).getDamageValue());
        assertEquals(64, remainders.get(1).getCount());
        assertEquals(Items.COBBLESTONE, remainders.get(1).getItem());
    }

}
