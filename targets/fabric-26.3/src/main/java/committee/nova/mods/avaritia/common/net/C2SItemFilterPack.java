package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.iface.IFilterItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import committee.nova.mods.avaritia.api.common.net.PacketContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Supplier;

/**
 * C2SJEIGhostPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public class C2SItemFilterPack {
    private static final RegistryOps<Tag> STACK_OPS = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
            .createSerializationContext(NbtOps.INSTANCE);

    private final int action;
    private final ItemStack stack;

    public C2SItemFilterPack(FriendlyByteBuf buf) {
        this.action = buf.readInt();
        CompoundTag tag = buf.readNbt();
        this.stack = tag == null ? ItemStack.EMPTY
                : ItemStack.OPTIONAL_CODEC.parse(STACK_OPS, tag).result().orElse(ItemStack.EMPTY);
    }

    public C2SItemFilterPack(int action, ItemStack stack) {
        this.action = action;
        this.stack = stack;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(action);
        Tag tag = ItemStack.OPTIONAL_CODEC.encodeStart(STACK_OPS, this.stack).getOrThrow(IllegalArgumentException::new);
        buf.writeNbt(tag);
    }

    public void run(Supplier<PacketContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || action < 0 || action > 2 || (action != 2 && stack.isEmpty())) return;
            ItemStack toolStack = player.getMainHandItem();
            if (toolStack.getItem() instanceof IFilterItem) {
                CustomData custom = toolStack.get(DataComponents.CUSTOM_DATA);
                CompoundTag tag = custom == null ? new CompoundTag() : custom.copyTag();
                String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                CompoundTag filters = tag.getCompoundOrEmpty("filters");
                switch (action) {
                    case 0 -> {
                        if (!filters.contains(key)) {
                            filters.put(key, ItemStack.OPTIONAL_CODEC.encodeStart(STACK_OPS, stack).getOrThrow(IllegalArgumentException::new));
                        }
                        tag.put("filters", filters);
                    }
                    case 1 -> {
                        filters.remove(key);
                        tag.put("filters", filters);
                    }
                    case 2 -> tag.remove("filters");
                }
                toolStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }

        });
        ctx.get().setPacketHandled(true);
    }
}
