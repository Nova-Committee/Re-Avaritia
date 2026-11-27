package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.Const;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * @author cnlimiter
 */
@Mod.EventBusSubscriber(modid = Const.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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

    @SubscribeEvent
    public static void onLoggingInServer(ClientPlayerNetworkEvent.LoggingIn event) {
        newInstance();
    }

    @SubscribeEvent
    public void onLoggingOutServer(ClientPlayerNetworkEvent.LoggingOut event) {
        MinecraftForge.EVENT_BUS.unregister(this);
        instance = null;
    }


    private CompoundTag userCache;
    private final ClientChestHandler channel = new ClientChestHandler();


    public ClientChestManager() {
        MinecraftForge.EVENT_BUS.register(this);
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
