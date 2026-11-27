package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.core.chest.ItemSuper;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InfinityChestPayloadCodecTest {

    @Test
    void roundTripsLongCounts() {
        RegistryFriendlyByteBuf buffer = buffer();
        try {
            long count = (long) Integer.MAX_VALUE + 42L;
            S2CInfinityChestStatePack packet = new S2CInfinityChestStatePack(
                    ChannelState.FULL, List.of(new ItemSuper(new ItemStack(Items.STONE), count)));

            S2CInfinityChestStatePack.STREAM_CODEC.encode(buffer, packet);
            S2CInfinityChestStatePack decoded = S2CInfinityChestStatePack.STREAM_CODEC.decode(buffer);

            assertEquals(ChannelState.FULL, decoded.channelState());
            assertEquals(count, decoded.items().iterator().next().getRealCount());
        } finally {
            buffer.release();
        }
    }

    @Test
    void normalizesMaliciousWireStackCount() {
        RegistryFriendlyByteBuf buffer = buffer();
        try {
            buffer.writeInt(7);
            buffer.writeInt(4);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, new ItemStack(Items.STONE, 64));
            buffer.writeLong(Long.MAX_VALUE);

            C2SInfinityChestActionPack decoded = C2SInfinityChestActionPack.STREAM_CODEC.decode(buffer);

            assertEquals(1, decoded.itemSuper().getStack().getCount());
            assertEquals(Long.MAX_VALUE, decoded.itemSuper().getRealCount());
        } finally {
            buffer.release();
        }
    }

    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(
                Unpooled.buffer(), RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }
}
