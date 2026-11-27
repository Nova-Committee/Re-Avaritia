package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.common.container.InfinityChestContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Collection;

/** 客户端连接级无尽箱状态。 */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public final class ClientChestManager {
    private static final ClientChestManager INSTANCE = new ClientChestManager();

    private final ClientChestHandler channel = new ClientChestHandler();

    private ClientChestManager() {
    }

    public static ClientChestManager getInstance() {
        return INSTANCE;
    }

    public ClientChestHandler getChest(InfinityChestContainer container) {
        channel.addListener(container);
        return channel;
    }

    public void fullUpdatePage(Collection<ChestHandler.StoredItem> entries, boolean reset, boolean last) {
        channel.fullUpdatePage(entries, reset, last);
    }

    public void update(Collection<ChestHandler.StoredItem> changed, Collection<ItemResource> removed) {
        channel.update(changed, removed);
    }

    public void close(InfinityChestContainer container) {
        channel.removeListener(container);
    }

    @SubscribeEvent
    public static void loggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        INSTANCE.channel.clear();
    }

    @SubscribeEvent
    public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        INSTANCE.channel.clear();
    }
}
