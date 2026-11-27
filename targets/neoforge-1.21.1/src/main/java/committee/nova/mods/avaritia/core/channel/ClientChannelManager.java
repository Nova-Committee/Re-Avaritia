package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.Const;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public final class ClientChannelManager {
    private static final ClientChannelManager INSTANCE = new ClientChannelManager();

    private CompoundTag userCache = new CompoundTag();
    private final ClientChannel channel = new ClientChannel();
    public final HashMap<Integer, String> myChannels = new HashMap<>();
    public final HashMap<Integer, String> otherChannels = new HashMap<>();
    public final HashMap<Integer, String> publicChannels = new HashMap<>();
    public byte selectedChannelType = -1;
    public int selectedChannelID = -1;
    public String selectedChannelName = "";
    @Nullable
    private Runnable screenRefresh;

    private ClientChannelManager() {
    }

    public static ClientChannelManager getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        INSTANCE.clearConnectionState();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        INSTANCE.clearConnectionState();
    }

    public void setUserCache(CompoundTag userCache) {
        this.userCache = userCache == null ? new CompoundTag() : userCache;
    }

    public CompoundTag getUserCache() {
        return userCache;
    }

    public String getUserName(UUID uuid) {
        String userName = userCache.getCompound("nameCache").getString(uuid.toString());
        return userName.isEmpty() ? "unknownUser" : userName;
    }

    public ClientChannel getChannel() {
        return channel;
    }

    public ClientChannel getChannel(committee.nova.mods.avaritia.common.container.DummyChannelContainer container) {
        channel.addListener(container);
        return channel;
    }

    public void updateChannel(CompoundTag data) {
        channel.update(data);
    }

    public void fullUpdateChannel(CompoundTag data) {
        channel.fullUpdate(data);
    }

    public void setChannelList(CompoundTag mine, CompoundTag other, CompoundTag shared) {
        readList(mine, myChannels);
        readList(other, otherChannels);
        readList(shared, publicChannels);
        refreshScreen();
    }

    private static void readList(CompoundTag source, HashMap<Integer, String> target) {
        target.clear();
        source.getAllKeys().forEach(key -> {
            try {
                int id = Integer.parseInt(key);
                if (id >= 0 && id < 10_000) {
                    target.put(id, source.getString(key));
                }
            } catch (NumberFormatException ignored) {
            }
        });
    }

    public void addChannel(byte type, int id, String name) {
        channelList(type).put(id, name);
        refreshScreen();
    }

    public void removeChannel(byte type, int id, String name) {
        channelList(type).remove(id);
        refreshScreen();
    }

    private HashMap<Integer, String> channelList(byte type) {
        return switch (type) {
            case 0 -> myChannels;
            case 1 -> otherChannels;
            case 2 -> publicChannels;
            default -> new HashMap<>();
        };
    }

    public void setSelectedChannel(byte type, int id, String name) {
        selectedChannelType = type;
        selectedChannelID = id;
        selectedChannelName = name;
        refreshScreen();
    }

    public void addScreenRefresh(Runnable listener) {
        screenRefresh = listener;
    }

    private void refreshScreen() {
        if (screenRefresh != null) {
            screenRefresh.run();
        }
    }

    public void onScreenClose() {
        screenRefresh = null;
        otherChannels.clear();
        selectedChannelType = -1;
        selectedChannelID = -1;
        selectedChannelName = "";
    }

    private void clearConnectionState() {
        channel.removeListener();
        userCache = new CompoundTag();
        myChannels.clear();
        otherChannels.clear();
        publicChannels.clear();
        screenRefresh = null;
        selectedChannelType = -1;
        selectedChannelID = -1;
        selectedChannelName = "";
    }
}
