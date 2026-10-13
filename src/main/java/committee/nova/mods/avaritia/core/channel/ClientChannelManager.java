package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.Const;
import committee.nova.mods.avaritia.client.screen.TesseractChannelScreen;
import committee.nova.mods.avaritia.common.container.DummyChannelContainer;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2025/2/28 12:32
 * @Description:
 */
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

    public static void onLoggingInServer() {
        newInstance();
    }

    public static void onLoggingOutServer() {
        instance = null;
    }


    private CompoundTag userCache = new CompoundTag();
    private final ClientChannel channel = new ClientChannel();
    public final HashMap<Integer, String> myChannels = new HashMap<>();
    public final HashMap<Integer, String> otherChannels = new HashMap<>();
    public final HashMap<Integer, String> publicChannels = new HashMap<>();
    public byte selectedChannelType = -1;
    public int selectedChannelID = -1;
    public String selectedChannelName = "";
    private Runnable selectorListener = () -> { };
    @Nullable
    private TesseractChannelScreen screen;


    public ClientChannelManager() {
    }

    public void setUserCache(CompoundTag userCache) {
        this.userCache = userCache;
    }

    public CompoundTag getUserCache() {
        return userCache;
    }

    public String getUserName(UUID uuid) {
        String userName = userCache.getCompoundOrEmpty("nameCache").getStringOr(uuid.toString(), "");
        if (userName.equals("")) return "unknownUser";
        return userName;
    }

    public ClientChannel getChannel() {
        return channel;
    }

    public ClientChannel getChannel(DummyChannelContainer container) {
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
        my.keySet().forEach(s -> myChannels.put(Integer.parseInt(s), my.getStringOr(s, "")));
        other.keySet().forEach(s -> otherChannels.put(Integer.parseInt(s), other.getStringOr(s, "")));
        pub.keySet().forEach(s -> publicChannels.put(Integer.parseInt(s), pub.getStringOr(s, "")));
        //if (screen != null) screen.updateChannelList();
    }

    public void addChannel(byte type, int id, String name) {
        switch (type) {
            case (byte) 0 -> myChannels.put(id, name);
            case (byte) 1 -> otherChannels.put(id, name);
            case (byte) 2 -> publicChannels.put(id, name);
        }
        if (screen != null) screen.refreshChannels();
        selectorListener.run();
    }

    public void removeChannel(byte type, int id, String name) {
        switch (type) {
            case (byte) 0 -> myChannels.remove(id);
            case (byte) 1 -> otherChannels.remove(id);
            case (byte) 2 -> publicChannels.remove(id);
        }
        if (screen != null) screen.refreshChannels();
        selectorListener.run();
    }

    public void setSelectedChannel(byte type, int ID, String name) {
        selectedChannelType = type;
        selectedChannelID = ID;
        selectedChannelName = name;
    }

    /** Neo 版同款只读视图：我的 / 终端所有者 / 共享频道列表。 */
    public Map<Integer, String> mine() {
        return Map.copyOf(myChannels);
    }

    public Map<Integer, String> terminalOwner() {
        return Map.copyOf(otherChannels);
    }

    public Map<Integer, String> shared() {
        return Map.copyOf(publicChannels);
    }

    public byte selectedType() {
        return selectedChannelType;
    }

    public int selectedId() {
        return selectedChannelID;
    }

    public void listenSelector(Runnable listener) {
        this.selectorListener = listener == null ? () -> { } : listener;
    }

    public void closeSelector() {
        this.selectorListener = () -> { };
        this.selectedChannelType = -1;
        this.selectedChannelID = -1;
        this.selectedChannelName = "";
    }

    public void addScreen(TesseractChannelScreen screen) {
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
