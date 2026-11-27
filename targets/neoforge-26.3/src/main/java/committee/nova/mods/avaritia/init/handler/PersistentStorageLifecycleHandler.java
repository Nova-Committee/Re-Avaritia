package committee.nova.mods.avaritia.init.handler;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.core.channel.ServerChannelManager;
import committee.nova.mods.avaritia.core.chest.ServerChestManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Releases server-bound storage listeners and selector state after shutdown. */
@EventBusSubscriber(modid = Const.MOD_ID)
public final class PersistentStorageLifecycleHandler {
    private PersistentStorageLifecycleHandler() {
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ServerChannelManager.release(event.getServer());
        ServerChestManager.release(event.getServer());
    }
}
