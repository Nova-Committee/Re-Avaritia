package committee.nova.mods.avaritia.common.net.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.menu.TesseractChannelMenu;
import committee.nova.mods.avaritia.common.menu.TesseractMenu;
import committee.nova.mods.avaritia.core.channel.ServerChannelManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

final class ChannelPayloadGuard {
    private ChannelPayloadGuard() { }

    static @Nullable TesseractMenu mainMenu(IPayloadContext context, int containerId) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.containerMenu instanceof TesseractMenu menu)
                || menu.containerId != containerId
                || !menu.stillValid(player)
                || !menu.canPlayerModify(player)) {
            reject(context, "main");
            return null;
        }
        return menu;
    }

    static @Nullable TesseractChannelMenu selectorMenu(IPayloadContext context, int containerId) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.containerMenu instanceof TesseractChannelMenu menu)
                || menu.containerId != containerId
                || menu.terminal == null
                || !menu.stillValid(player)
                || !menu.canPlayerModify(player)) {
            reject(context, "selector");
            return null;
        }
        ServerChannelManager manager = ServerChannelManager.getInstance();
        if (manager == null || !manager.isSelecting(player, menu.getTerminalOwner())) {
            reject(context, "selector binding");
            return null;
        }
        return menu;
    }

    private static void reject(IPayloadContext context, String operation) {
        Const.LOGGER.debug("Rejected invalid Tesseract {} payload from {}", operation, context.player().getUUID());
    }
}
