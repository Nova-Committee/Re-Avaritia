package committee.nova.mods.avaritia.common.item.tools;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/** 长矛锁定模式共用的服务端近身移动。 */
public final class SpearThrustUtils {
    private static final double TARGET_GAP = 0.25D;
    private static final double MIN_DIRECTION_LENGTH_SQR = 1.0E-6D;

    private SpearThrustUtils() {
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
}
