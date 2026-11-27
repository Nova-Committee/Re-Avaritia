package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.Const;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.HashMap;
import java.util.Map;

/** Connection-scoped client cache used by both Tesseract screens. */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public final class ClientChannelManager {
    private static final ClientChannelManager INSTANCE = new ClientChannelManager();

    private final ClientChannel channel = new ClientChannel();
    private final Map<Integer, String> mine = new HashMap<>();
    private final Map<Integer, String> terminalOwner = new HashMap<>();
    private final Map<Integer, String> shared = new HashMap<>();
    private Runnable selectorListener = () -> { };
    private byte selectedType = -1;
    private int selectedId = -1;
    private String selectedName = "";

    private ClientChannelManager() {
    }

    public static ClientChannelManager getInstance() {
        return INSTANCE;
    }

    public ClientChannel channel() {
        return channel;
    }

    public Map<Integer, String> mine() {
        return Map.copyOf(mine);
    }

    public Map<Integer, String> terminalOwner() {
        return Map.copyOf(terminalOwner);
    }

    public Map<Integer, String> shared() {
        return Map.copyOf(shared);
    }

    public byte selectedType() {
        return selectedType;
    }

    public int selectedId() {
        return selectedId;
    }

    public String selectedName() {
        return selectedName;
    }

    public void replaceLists(Map<Integer, String> mine, Map<Integer, String> terminalOwner, Map<Integer, String> shared) {
        replace(this.mine, mine);
        replace(this.terminalOwner, terminalOwner);
        replace(this.shared, shared);
        selectorListener.run();
    }

    public void applyAction(committee.nova.mods.avaritia.common.net.channel.ChannelAction action,
                            byte type, int id, String name) {
        if (action == committee.nova.mods.avaritia.common.net.channel.ChannelAction.SET) {
            selectedType = type;
            selectedId = id;
            selectedName = name;
        } else {
            Map<Integer, String> target = list(type);
            if (action == committee.nova.mods.avaritia.common.net.channel.ChannelAction.ADD) target.put(id, name);
            if (action == committee.nova.mods.avaritia.common.net.channel.ChannelAction.REMOVE) target.remove(id);
        }
        selectorListener.run();
    }

    public void listenSelector(Runnable listener) {
        selectorListener = listener == null ? () -> { } : listener;
    }

    public void closeSelector() {
        selectorListener = () -> { };
        terminalOwner.clear();
        selectedType = -1;
        selectedId = -1;
        selectedName = "";
    }

    @SubscribeEvent
    public static void loggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        INSTANCE.clear();
    }

    @SubscribeEvent
    public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        INSTANCE.clear();
    }

    private static void replace(Map<Integer, String> target, Map<Integer, String> source) {
        target.clear();
        if (source != null) target.putAll(source);
    }

    private Map<Integer, String> list(byte type) {
        return switch (type) {
            case 0 -> mine;
            case 1 -> terminalOwner;
            case 2 -> shared;
            default -> new HashMap<>();
        };
    }

    private void clear() {
        mine.clear();
        terminalOwner.clear();
        shared.clear();
        channel.replace(null);
        channel.clearListener();
        selectorListener = () -> { };
        selectedType = -1;
        selectedId = -1;
        selectedName = "";
    }
}
