package committee.nova.mods.avaritia.common.net;

import committee.nova.mods.avaritia.init.config.ModConfig;
import committee.nova.mods.avaritia.util.InfinityElytraUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SElytraSpeedUpPacket {
    private static final double TAKEOFF_UPWARD_SPEED = 0.72D;
    private static final double TAKEOFF_FORWARD_SPEED = 0.35D;
    private static final double BOOST_TAKEOFF_FORWARD_SPEED = 0.65D;

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
            if (!InfinityElytraUtils.hasInfinityElytraEquipped(player)) return;
            if (!customFlying) return;

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
                && !player.hasEffect(MobEffects.LEVITATION);
    }

    private static void launchFromGround(ServerPlayer player, boolean boosting) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(lookVec.x, 0.0D, lookVec.z);
        Vec3 forward = horizontalLook.lengthSqr() > 1.0E-7D ? horizontalLook.normalize() : Vec3.ZERO;
        double forwardSpeed = boosting ? BOOST_TAKEOFF_FORWARD_SPEED : TAKEOFF_FORWARD_SPEED;
        Vec3 currentVelocity = player.getDeltaMovement();

        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

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
