package committee.nova.mods.avaritia.common.item.tools;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** 长矛远距突刺共用的选敌、移动与伤害上下文。 */
public final class SpearThrustUtils {
    private static final double TARGET_GAP = 0.25D;
    private static final double MIN_DIRECTION_LENGTH_SQR = 1.0E-6D;
    private static final ThreadLocal<RemoteAttackContext> REMOTE_ATTACK = new ThreadLocal<>();

    private SpearThrustUtils() {
    }

    public static @Nullable LivingEntity selectRandomTarget(ServerLevel level, ServerPlayer player,
                                                             double range, int poolSize) {
        AABB searchBox = player.getBoundingBox().inflate(range);
        List<LivingEntity> nearestTargets = level.getEntitiesOfClass(
                        LivingEntity.class,
                        searchBox,
                        target -> isEligibleTarget(player, target))
                .stream()
                .sorted(Comparator.comparingDouble(player::distanceToSqr))
                .limit(poolSize)
                .toList();
        if (nearestTargets.isEmpty()) {
            return null;
        }
        return nearestTargets.get(level.getRandom().nextInt(nearestTargets.size()));
    }

    public static boolean isEligibleTarget(Player player, LivingEntity target) {
        return target != player
                && target.isAlive()
                && !target.isRemoved()
                && !target.isSpectator()
                && !(target instanceof ArmorStand)
                && !player.isAlliedTo(target)
                && !(target instanceof Player targetPlayer && targetPlayer.isCreative());
    }

    public static boolean stabTarget(ServerPlayer player, InteractionHand hand, LivingEntity target) {
        float multiplier = SpearMarkUtils.isMarkedBy(target, player) ? 2.0F : 1.0F;
        RemoteAttackContext previous = REMOTE_ATTACK.get();
        REMOTE_ATTACK.set(new RemoteAttackContext(player.getUUID(), target.getUUID(), multiplier));
        try {
            float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * multiplier;
            return player.stabAttack(hand.asEquipmentSlot(), target, damage, true, false, false);
        } finally {
            if (previous == null) {
                REMOTE_ATTACK.remove();
            } else {
                REMOTE_ATTACK.set(previous);
            }
        }
    }

    public static float remoteDamageMultiplier(LivingEntity attacker, LivingEntity target) {
        RemoteAttackContext context = REMOTE_ATTACK.get();
        if (context == null
                || !context.attackerId.equals(attacker.getUUID())
                || !context.targetId.equals(target.getUUID())) {
            return 1.0F;
        }
        return context.damageMultiplier;
    }

    public static boolean movePlayerToTarget(ServerLevel level, ServerPlayer player, LivingEntity target) {
        Vec3 toTarget = target.position().subtract(player.position());
        Vec3 horizontalDirection = new Vec3(toTarget.x, 0.0D, toTarget.z);
        if (horizontalDirection.lengthSqr() < MIN_DIRECTION_LENGTH_SQR) {
            horizontalDirection = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontalDirection = horizontalDirection.normalize();
        }

        double targetDistance = (player.getBbWidth() + target.getBbWidth()) * 0.5D + TARGET_GAP;
        EntityDimensions playerDimensions = player.getDimensions(player.getPose());
        for (int quarterTurn = 0; quarterTurn < 4; quarterTurn++) {
            Vec3 approachDirection = horizontalDirection.yRot(Mth.HALF_PI * quarterTurn);
            Vec3 destination = target.position().subtract(approachDirection.scale(targetDistance));
            if (!level.noCollision(player, playerDimensions.makeBoundingBox(destination))) {
                continue;
            }
            if (!player.teleportTo(level, destination.x, destination.y, destination.z,
                    Set.of(), player.getYRot(), player.getXRot(), false)) {
                continue;
            }

            player.setDeltaMovement(Vec3.ZERO);
            player.resetFallDistance();
            player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
            return true;
        }
        return false;
    }

    private record RemoteAttackContext(UUID attackerId, UUID targetId, float damageMultiplier) {
    }
}
