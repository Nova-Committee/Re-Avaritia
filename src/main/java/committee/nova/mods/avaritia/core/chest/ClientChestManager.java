package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.Const;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.Collection;
import java.util.UUID;

/**
 * @author cnlimiter
 */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class ClientChestManager {
    private static volatile ClientChestManager instance;

    public static ClientChestManager getInstance() {
        if (instance == null) {
            synchronized (ClientChestManager.class) {
                if (instance == null) instance = new ClientChestManager();
            }
        }
        return instance;
    }

    @SubscribeEvent
    public static void onLoggingInServer(ClientPlayerNetworkEvent.LoggingIn event) {
        if (instance == null) {
            synchronized (ClientChestManager.class) {
                if (instance == null) instance = new ClientChestManager();
            }
        }
    }

    @SubscribeEvent
    public static void onLoggingOutServer(ClientPlayerNetworkEvent.LoggingOut event) {
        if (instance != null) {
            instance.channel.removeListener();
        }
        instance = null;
    }


    private CompoundTag userCache;
    private final ClientChestHandler channel = new ClientChestHandler();


    public ClientChestManager() {}

    public void setUserCache(CompoundTag userCache) {
        this.userCache = userCache;
    }

    public CompoundTag getUserCache() {
        return userCache;
    }

    public String getUserName(UUID uuid) {
        if (userCache == null) return "unknownUser";
        String userName = userCache.getCompound("nameCache").getString(uuid.toString());
        if (userName.isEmpty()) return "unknownUser";
        return userName;
    }

    public ClientChestHandler getChest() {
        return channel;
    }

    public ClientChestHandler getChest(InfinityChestContainer container) {
        channel.addListener(container);
        return channel;
    }

    public void updateChest(Collection<ItemSuper> data) {
        channel.update(data);
    }

    public void fullUpdateChest(Collection<ItemSuper> data) {
        channel.fullUpdate(data);
    }
}
