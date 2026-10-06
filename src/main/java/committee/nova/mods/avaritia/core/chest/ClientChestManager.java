package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.Const;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * @author cnlimiter
 */
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

    private static void newInstance() {
        if (instance == null) {
            synchronized (ClientChestManager.class) {
                if (instance == null) instance = new ClientChestManager();
            }
        }
    }

    public static void onLoggingInServer() {
        newInstance();
    }

    public static void onLoggingOutServer() {
        instance = null;
    }


    private CompoundTag userCache = new CompoundTag();
    private final ClientChestHandler channel = new ClientChestHandler();


    public ClientChestManager() {
    }

    public void setUserCache(CompoundTag userCache) {
        this.userCache = userCache;
    }

    public CompoundTag getUserCache() {
        return userCache;
    }

    public String getUserName(UUID uuid) {
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

    public void updateChest(CompoundTag data) {
        channel.update(data);
    }

    public void fullUpdateChest(CompoundTag data) {
        channel.fullUpdate(data);
    }
}
