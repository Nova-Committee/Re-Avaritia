package committee.nova.mods.avaritia.common.net;




import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;


import net.minecraft.world.item.ItemStack;


import committee.nova.mods.avaritia.api.common.net.PacketContext;

import java.util.function.Supplier;

/**
 * S2CTotemPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public class S2CTotemPack {
    private static final RegistryOps<Tag> STACK_OPS = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
            .createSerializationContext(NbtOps.INSTANCE);

    public final ItemStack stack;
    public final int entityId;

    public S2CTotemPack(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        this.stack = tag == null ? ItemStack.EMPTY
                : ItemStack.OPTIONAL_CODEC.parse(STACK_OPS, tag).result().orElse(ItemStack.EMPTY);
        this.entityId = buf.readInt();
    }

    public S2CTotemPack(ItemStack stack, int entityId) {
        this.stack = stack;
        this.entityId = entityId;
    }

    public void write(FriendlyByteBuf buf) {
        Tag tag = ItemStack.OPTIONAL_CODEC.encodeStart(STACK_OPS, this.stack).getOrThrow(IllegalArgumentException::new);
        buf.writeNbt(tag);
        buf.writeInt(this.entityId);
    }

    public void run(Supplier<PacketContext> ctx) { ctx.get().enqueueWork(() -> committee.nova.mods.avaritia.common.net.ClientPacketProxy.handle(this)); ctx.get().setPacketHandled(true); }
}
