package committee.nova.mods.avaritia.api.utils.math;

import net.minecraft.world.level.Level;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 13:55
 * Version: 1.0
 */
public class TimeTracer {
    private long lastMark = Long.MIN_VALUE;

    public boolean hasDelayPassed(Level world, int delay) {

        long currentTime = world.getGameTime();

        if (currentTime < lastMark) {
            lastMark = currentTime;
            return false;
        } else if (lastMark + delay <= currentTime) {
            lastMark = currentTime;
            return true;
        }
        return false;
    }

    public void markTime(Level world) {

        lastMark = world.getGameTime();
    }
}
