package nova.committee.avaritia.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 11:50
 * Version: 1.0
 */
public class RayTracer {

    public static Vec3 getCorrectedHeadVec(Player player) {
        return player.position().add(0.0D, (double) player.getEyeHeight(), 0.0D);
    }

    public static Vec3 getStartVec(Player player) {
        return getCorrectedHeadVec(player);
    }


    public static Vec3 getEndVec(Player player) {
        Vec3 headVec = getCorrectedHeadVec(player);
        Vec3 lookVec = player.getViewVector(2F);
        double reach = 5.0;
        return headVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
    }

    public static Vec3 getEndVec(Player player, double reach) {
        Vec3 headVec = getCorrectedHeadVec(player);
        Vec3 lookVec = player.getViewVector(2F);
        return headVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
    }

    public static BlockHitResult retrace(Player player, double reach) {
        Vec3 startVec = getStartVec(player);
        Vec3 endVec = getEndVec(player, reach);
        ClipContext rayTraceContext = new ClipContext(startVec, endVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        return player.level.clip(rayTraceContext);
    }

}
