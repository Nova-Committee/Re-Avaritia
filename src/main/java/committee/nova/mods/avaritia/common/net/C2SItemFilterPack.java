package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.api.iface.IFilterItem;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/** Serverbound edits to a held tool's registry-keyed custom-data filters. */
public record C2SItemFilterPack(ItemStack stack, int action) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2SItemFilterPack> TYPE = new CustomPacketPayload.Type<>(Const.rl("c2s_item_filter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SItemFilterPack> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC,
            C2SItemFilterPack::stack,
            ByteBufCodecs.INT,
            C2SItemFilterPack::action,
            C2SItemFilterPack::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SItemFilterPack> {
        @Override
        public void handle(@NotNull C2SItemFilterPack packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)) {
                    return;
                }
                ItemStack tool = player.getMainHandItem();
                if (!(tool.getItem() instanceof IFilterItem) || packet.action < 0 || packet.action > 2
                        || packet.action != 2 && packet.stack.isEmpty()) {
                    return;
                }
                CompoundTag stored = tool.get(ModDataComponents.TOOL_FILTERS.get());
                String itemId = BuiltInRegistries.ITEM.getKey(packet.stack.getItem()).toString();
                if (packet.action == 0 && stored != null && stored.contains(itemId)
                        || packet.action == 1 && (stored == null || !stored.contains(itemId))
                        || packet.action == 2 && (stored == null || stored.isEmpty())) {
                    return;
                }
                CompoundTag updated = stored == null || packet.action == 2 ? new CompoundTag() : stored.copy();
                switch (packet.action) {
                    case 0 -> updated.put(itemId, packet.stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
                    case 1 -> updated.remove(itemId);
                }
                tool.set(ModDataComponents.TOOL_FILTERS.get(), updated);
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
            });
        }
    }
}
