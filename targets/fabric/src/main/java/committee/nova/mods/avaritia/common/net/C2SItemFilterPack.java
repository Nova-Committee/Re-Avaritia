package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.api.iface.IFilterItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import committee.nova.mods.avaritia.api.common.net.PacketContext;
import net.minecraft.core.registries.BuiltInRegistries;

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
    private final int action;
    private final ItemStack stack;

    public C2SItemFilterPack(FriendlyByteBuf buf) {
        this.action = buf.readInt();
        this.stack = buf.readItem();
    }

    public C2SItemFilterPack(int action, ItemStack stack) {
        this.action = action;
        this.stack = stack;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(action);
        buf.writeItem(stack);
    }

    public void run(Supplier<PacketContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || action < 0 || action > 2 || (action != 2 && stack.isEmpty())) return;
            if (player.getMainHandItem().getItem() instanceof IFilterItem) {
                var tag = player.getMainHandItem().getOrCreateTag();
                switch (action) {
                    case 0 -> {
                        if (tag.contains("filters")) {
                            if (!tag.getCompound("filters").contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                                tag.getCompound("filters")
                                        .put(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.save(new CompoundTag()));
                            }
                        } else {
                            CompoundTag filters = new CompoundTag();
                            filters.put(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.save(new CompoundTag()));
                            tag.put("filters", filters);
                        }

                    }
                    case 1 -> {
                        CompoundTag filters = tag.getCompound("filters");
                        if (filters.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                            filters.remove(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                        }
                    }
                    case 2 -> {
                        tag.remove("filters");
                    }
                }

            }

        });
        ctx.get().setPacketHandled(true);
    }
}
