package committee.nova.mods.avaritia.common.net.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.InfinityChestMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/** 所有无尽箱 C2S 载荷共用的菜单绑定、距离和权限校验。 */
final class InfinityChestPayloadGuard {
    private InfinityChestPayloadGuard() {
    }

    static @Nullable InfinityChestMenu menu(IPayloadContext context, int containerId) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.containerMenu instanceof InfinityChestMenu menu)
                || menu.containerId != containerId
                || !menu.stillValid(player)
                || !menu.canPlayerModify(player)) {
            Const.LOGGER.debug("Rejected invalid infinity chest payload from {}", context.player().getUUID());
            return null;
        }
        return menu;
    }
}
