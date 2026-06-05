package com.avaritia.common.net;

import com.avaritia.Const;
import com.avaritia.api.iface.IFilterItem;
import com.avaritia.init.registry.ModDataComponents;
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

/**
 * C2SJEIGhostPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public record C2SItemFilterPacket(ItemStack stack, int action) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2SItemFilterPacket> TYPE = new CustomPacketPayload.Type<>(Const.rl("c2s_item_filter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SItemFilterPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC,
            C2SItemFilterPacket::stack,
            ByteBufCodecs.INT,
            C2SItemFilterPacket::action,
            C2SItemFilterPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SItemFilterPacket> {
        @Override
        public void handle(@NotNull C2SItemFilterPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                var player = context.player();
                if (player instanceof ServerPlayer serverPlayer) {
                    ItemStack toolStack = serverPlayer.getMainHandItem();
                    if (toolStack.getItem() instanceof IFilterItem) {
                        CompoundTag filters = toolStack.getOrDefault(ModDataComponents.TOOL_FILTERS.get(), new CompoundTag());
                        toolStack.set(ModDataComponents.TOOL_FILTERS.get(), mutateFilterTag(filters, packet.stack, packet.action));
                    }
                }
            });
        }
    }

    public static CompoundTag mutateFilterTag(CompoundTag currentFilters, ItemStack filterStack, int action) {
        CompoundTag filters = currentFilters == null ? new CompoundTag() : currentFilters.copy();
        if (action == 2) {
            return new CompoundTag();
        }
        if (filterStack == null || filterStack.isEmpty()) {
            return filters;
        }

        String key = BuiltInRegistries.ITEM.getKey(filterStack.getItem()).toString();
        CustomData customData = filterStack.get(DataComponents.CUSTOM_DATA);
        return ItemFilterTags.mutate(currentFilters, key, customData == null ? null : customData.copyTag(), action);
    }

    public static CompoundTag mutateFilterTag(CompoundTag currentFilters, String key, CompoundTag customData, int action) {
        return ItemFilterTags.mutate(currentFilters, key, customData, action);
    }
}
