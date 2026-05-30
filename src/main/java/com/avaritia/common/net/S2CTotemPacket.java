package com.avaritia.common.net;

import com.avaritia.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

/**
 * S2CTotemPacket
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 14:02
 */
public record S2CTotemPacket(ItemStack stack, int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CTotemPacket> TYPE = new CustomPacketPayload.Type<>(Const.rl("s2c_totem"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CTotemPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC,
            S2CTotemPacket::stack,
            ByteBufCodecs.INT,
            S2CTotemPacket::entityId,
            S2CTotemPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<S2CTotemPacket> {
        @Override
        public void handle(@NotNull S2CTotemPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                Minecraft instance = Minecraft.getInstance();
                ClientLevel world = instance.level;

                if (world != null) {
                    Entity entity = world.getEntity(packet.entityId);
                    if (entity != null) {
                        instance.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                        world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
                        instance.gameRenderer.displayItemActivation(packet.stack);
                    }
                }
            });
        }
    }

}
