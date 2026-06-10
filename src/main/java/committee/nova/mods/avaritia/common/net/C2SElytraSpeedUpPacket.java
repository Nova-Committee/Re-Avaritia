package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.init.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record C2SElytraSpeedUpPacket(boolean customFlying, boolean boosting) implements CustomPacketPayload {
    private static final double TAKEOFF_UPWARD_SPEED = 0.72D;
    private static final double TAKEOFF_FORWARD_SPEED = 0.35D;
    private static final double BOOST_TAKEOFF_FORWARD_SPEED = 0.65D;

    public static final CustomPacketPayload.Type<C2SElytraSpeedUpPacket> TYPE = new CustomPacketPayload.Type<>(Const.rl("c2s_elytra_speed_up"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SElytraSpeedUpPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            C2SElytraSpeedUpPacket::customFlying,
            ByteBufCodecs.BOOL,
            C2SElytraSpeedUpPacket::boosting,
            C2SElytraSpeedUpPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class Handler implements IPayloadHandler<C2SElytraSpeedUpPacket> {
        @Override
        public void handle(@NotNull C2SElytraSpeedUpPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)) return;
                if (!player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.infinity_elytra.get())) return;
                if (!packet.customFlying()) return;

                boolean fallFlying = ensureFallFlying(player, packet.boosting());

                if (packet.boosting() && fallFlying) {
                    applyBoost(player);
                }
            }).exceptionally(e -> {
                context.disconnect(Component.literal("Error processing packet: " + e.getMessage()));
                return null;
            });
        }
    }

    private static boolean ensureFallFlying(ServerPlayer player, boolean boosting) {
        if (player.isFallFlying()) {
            return true;
        }

        if (player.tryToStartFallFlying()) {
            return true;
        }

        if (canLaunchFromGround(player)) {
            launchFromGround(player, boosting);
        }

        return false;
    }

    private static boolean canLaunchFromGround(ServerPlayer player) {
        return player.onGround()
                && !player.isPassenger()
                && !player.isInWater()
                && !player.onClimbable()
                && !player.getAbilities().flying
                && !player.hasEffect(MobEffects.LEVITATION);
    }

    private static void launchFromGround(ServerPlayer player, boolean boosting) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(lookVec.x, 0.0D, lookVec.z);
        double horizontalLengthSqr = horizontalLook.lengthSqr();
        Vec3 forward = horizontalLengthSqr > 1.0E-7D ? horizontalLook.normalize() : Vec3.ZERO;
        double forwardSpeed = boosting ? BOOST_TAKEOFF_FORWARD_SPEED : TAKEOFF_FORWARD_SPEED;
        Vec3 currentVelocity = player.getDeltaMovement();

        player.setDeltaMovement(
                currentVelocity.x + forward.x * forwardSpeed,
                Math.max(currentVelocity.y, TAKEOFF_UPWARD_SPEED),
                currentVelocity.z + forward.z * forwardSpeed
        );
        player.resetFallDistance();
        player.hurtMarked = true;

        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    player.getX(),
                    player.getY() + 0.15D,
                    player.getZ(),
                    10,
                    0.25D,
                    0.08D,
                    0.25D,
                    0.04D
            );
        }
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
