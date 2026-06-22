package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SElytraSpeedUpPacket {
    private final boolean customFlying;
    private final boolean boosting;

    public C2SElytraSpeedUpPacket(FriendlyByteBuf buf) {
        this.customFlying = buf.readBoolean();
        this.boosting = buf.readBoolean();
    }

    public C2SElytraSpeedUpPacket(boolean customFlying, boolean boosting) {
        this.customFlying = customFlying;
        this.boosting = boosting;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(customFlying);
        buf.writeBoolean(boosting);
    }

    public void run(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.infinity_elytra.get())) return;
            if (!customFlying) {
                stopFallFlying(player);
                return;
            }

            boolean fallFlying = ensureFallFlying(player, boosting);
            if (boosting && fallFlying) {
                applyBoost(player);
            }
        });
        context.setPacketHandled(true);
    }

    private static boolean ensureFallFlying(ServerPlayer player, boolean boosting) {
        if (player.isFallFlying()) {
            return true;
        }

        if (player.tryToStartFallFlying()) {
            return true;
        }

        return false;
    }

    private static void stopFallFlying(ServerPlayer player) {
        if (!player.isFallFlying()) {
            return;
        }

        player.stopFallFlying();
        player.resetFallDistance();
        player.hurtMarked = true;
    }

    private static void applyBoost(ServerPlayer player) {
        Vec3 lookVec = player.getLookAngle();
        double targetSpeed = Mth.clamp(ModConfig.infinityElytraFlyingSpeed.get(), 0.0D, 100.0D);
        Vec3 targetVelocity = lookVec.scale(targetSpeed);
        Vec3 nextVelocity = player.getDeltaMovement().lerp(targetVelocity, 0.35D);

        player.setDeltaMovement(nextVelocity);
        player.hurtMarked = true;

        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(
                    ParticleTypes.FLAME,
                    player.getX() - lookVec.x * 0.5,
                    player.getY() + player.getBbHeight() / 2,
                    player.getZ() - lookVec.z * 0.5,
                    6,
                    0.18D,
                    0.18D,
                    0.18D,
                    0.02D
            );
        }
    }
}
