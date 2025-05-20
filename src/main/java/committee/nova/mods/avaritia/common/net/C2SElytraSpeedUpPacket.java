package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record C2SElytraSpeedUpPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SElytraSpeedUpPacket> TYPE = new CustomPacketPayload.Type<>(Const.rl("c2s_elytra_speedup"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SElytraSpeedUpPacket> STREAM_CODEC = StreamCodec.unit(new C2SElytraSpeedUpPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SElytraSpeedUpPacket> {
        @Override
        public void handle(@NotNull C2SElytraSpeedUpPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                var player = context.player();
                if (player instanceof ServerPlayer serverPlayer){
                    if (serverPlayer.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.infinity_elytra.get()) && player.isFallFlying())
                        serverPlayer.serverLevel().addFreshEntity(new FireworkRocketEntity(serverPlayer.serverLevel(), Items.AIR.getDefaultInstance(), player));
                }
            });
        }
    }
}
