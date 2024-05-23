package committee.nova.mods.avaritia.util;

import net.minecraft.world.entity.player.Player;

/**
 * PlayerUtil
 *
 * @author cnlimiter
 * @version 1.0
 * @description
 * @date 2024/3/28 12:58
 */
public class PlayerUtil {
    public static boolean isPlayingMode(Player player) {
        return !player.isCreative() && !player.isSpectator();
    }
}
