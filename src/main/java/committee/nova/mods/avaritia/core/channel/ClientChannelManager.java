package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.screen.ChannelSelectScreen;
import committee.nova.mods.avaritia.common.container.ChannelDummyContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 12:32
 * @Description:
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Const.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientChannelManager {

    private static volatile ClientChannelManager instance;

    public static ClientChannelManager getInstance() {
        if (instance == null) {
            synchronized (ClientChannelManager.class) {
                if (instance == null) instance = new ClientChannelManager();
            }
        }
        return instance;
    }

    private static void newInstance() {
        if (instance == null) {
            synchronized (ClientChannelManager.class) {
                if (instance == null) instance = new ClientChannelManager();
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
    private final ClientChannel channel = new ClientChannel();
    public final HashMap<Integer, String> myChannels = new HashMap<>();
    public final HashMap<Integer, String> otherChannels = new HashMap<>();
    public final HashMap<Integer, String> publicChannels = new HashMap<>();
    public byte selectedChannelType = -1;
    public int selectedChannelID = -1;
    public String selectedChannelName = "";
    @Nullable
    private ChannelSelectScreen screen;


    public ClientChannelManager() {
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
        if (userName.equals("")) return "unknownUser";
        return userName;
    }

    public ClientChannel getChannel() {
        return channel;
    }

    public ClientChannel getChannel(ChannelDummyContainer container) {
        channel.addListener(container);
        return channel;
    }

    public void updateChannel(CompoundTag data) {
        channel.update(data);
    }

    public void fullUpdateChannel(CompoundTag data) {
        channel.fullUpdate(data);
    }

    public void setChannelList(CompoundTag my, CompoundTag other, CompoundTag pub) {
        myChannels.clear();
        otherChannels.clear();
        publicChannels.clear();
        my.getAllKeys().forEach(s -> myChannels.put(Integer.parseInt(s), my.getString(s)));
        other.getAllKeys().forEach(s -> otherChannels.put(Integer.parseInt(s), other.getString(s)));
        pub.getAllKeys().forEach(s -> publicChannels.put(Integer.parseInt(s), pub.getString(s)));
        //if (screen != null) screen.updateChannelList();
    }

    public void addChannel(byte type, int id, String name) {
        switch (type) {
            case (byte) 0 -> myChannels.put(id, name);
            case (byte) 1 -> otherChannels.put(id, name);
            case (byte) 2 -> publicChannels.put(id, name);
        }
        if (screen != null) screen.updateChannelList();
    }

    public void removeChannel(byte type, int id, String name) {
        switch (type) {
            case (byte) 0 -> myChannels.remove(id);
            case (byte) 1 -> otherChannels.remove(id);
            case (byte) 2 -> publicChannels.remove(id);
        }
        if (screen != null) screen.updateChannelList();
    }

    public void setSelectedChannel(byte type, int ID, String name) {
        selectedChannelType = type;
        selectedChannelID = ID;
        selectedChannelName = name;
    }

    public void addScreen(ChannelSelectScreen screen) {
        this.screen = screen;
    }

    public void onScreenClose() {
        screen = null;
        otherChannels.clear();
        selectedChannelType = -1;
        selectedChannelID = -1;
        selectedChannelName = "";
    }
}
