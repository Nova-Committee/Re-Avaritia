package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Static;
import committee.nova.mods.avaritia.api.iface.IFilterItem;
import committee.nova.mods.avaritia.init.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * C2SJEIGhostPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public record C2SItemFilterPack(ItemStack stack, int action) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2SItemFilterPack> TYPE = new CustomPacketPayload.Type<>(Static.rl("s2c_totem"));
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
                var player = context.player();
                if (player instanceof ServerPlayer serverPlayer) {
                    if (serverPlayer.getMainHandItem().getItem() instanceof IFilterItem) {
                        var tag = player.getMainHandItem().get(ModDataComponents.TOOL_FILTERS);
                        switch(packet.action) {
                            case 0 -> {
                                    if (!tag.contains(BuiltInRegistries.ITEM.getKey(packet.stack.getItem()).toString())){
                                        tag.getCompound("filters")
                                                .put(BuiltInRegistries.ITEM.getKey(packet.stack.getItem()).toString(), packet.stack.save());
                                    }


                            }
                            case 1 -> {
                                CompoundTag filters = tag.getCompound("filters");
                                if (filters.contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString())){
                                    filters.remove(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                                }
                            }
                            case 2 -> {
                                CompoundTag filters = tag.getCompound("filters");
                                filters.getAllKeys().forEach(filters::remove);
                            }
                        }

                    }
                }

            });
        }
    }
}
