package com.avaritia.network;

import com.avaritia.Avaritia;
import com.avaritia.api.iface.IFilterItem;
import com.avaritia.init.registry.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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

    public static final CustomPacketPayload.Type<C2SItemFilterPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.of(Avaritia.MOD_ID, "c2s_item_filter"));
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
                    if (serverPlayer.getMainHandItem().getItem() instanceof IFilterItem) {
                        var tag = player.getMainHandItem().getOrDefault(ModDataComponents.TOOL_FILTERS.get(), new CompoundTag());
                        switch(packet.action) {
                            case 0 -> {
                                if (!tag.contains(BuiltInRegistries.ITEM.getKey(packet.stack.getItem()).toString())){
                                    tag.put(BuiltInRegistries.ITEM.getKey(packet.stack.getItem()).toString(), packet.stack.get(DataComponents.CUSTOM_DATA).copyTag());
                                }
                            }
                            case 1 -> {
                                if (tag.contains(BuiltInRegistries.ITEM.getKey(packet.stack.getItem()).toString())){
                                    tag.remove(BuiltInRegistries.ITEM.getKey(packet.stack.getItem()).toString());
                                }
                            }
                            case 2 -> {
                                tag.getAllKeys().forEach(tag::remove);
                            }
                        }

                    }
                }

            });
        }
    }
}
