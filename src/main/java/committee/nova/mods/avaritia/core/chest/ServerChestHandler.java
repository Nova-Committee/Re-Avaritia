package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.common.net.channel.ChannelState;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;

/**
 * @author cnlimiter
 */
public class ServerChestHandler extends ChestHandler {
    private final HashSet<String> changedItems = new HashSet<>();
    private final HashSet<ServerPlayer> players = new HashSet<>();
    private boolean removed = false;

    public ServerChestHandler() {}

    public ServerChestHandler(CompoundTag dat) {
        initialize(dat);
    }

    @Override
    public void onItemChanged(String itemId, boolean listChanged) {
        super.onItemChanged(itemId, listChanged);
        changedItems.add(itemId);
    }

    public void initialize(CompoundTag dat) {
        storageItems.clear();
        componentDataCache.clear();
        nbtDataCache.clear();

        if (dat.contains("items")) {
            CompoundTag items = dat.getCompoundOrEmpty("items");
            items.keySet().forEach(itemId -> {
                long count = items.getLongOr(itemId, 0L);
                if (count > 0 && net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(Identifier.parse(StorageUtils.getBaseItemId(itemId)))) {
                    storageItems.put(itemId, count);
                }
            });
        }

        // 加载完整组件快照（新格式）
        if (dat.contains("components")) {
            CompoundTag components = dat.getCompoundOrEmpty("components");
            components.keySet().forEach(itemId -> {
                if (components.get(itemId) instanceof CompoundTag snapshot) {
                    componentDataCache.put(itemId, snapshot);
                }
            });
        }

        // 加载旧版NBT数据（向后兼容旧存档，仅含 CUSTOM_DATA 的条目）
        if (dat.contains("nbtData")) {
            CompoundTag nbtData = dat.getCompoundOrEmpty("nbtData");
            nbtData.keySet().forEach(itemId -> {
                Tag nbtTag = nbtData.get(itemId);
                if (nbtTag instanceof CompoundTag compoundTag && !componentDataCache.containsKey(itemId)) {
                    nbtDataCache.put(itemId, compoundTag);
                }
            });
        }

        updateItemKeys();
    }

    public void addListener(ServerPlayer player) {
        players.add(player);
        NetworkHandler.CHANNEL.sendTo(player, new S2CInfinityChestStatePack(ChannelState.FULL, buildData()));
    }

    public void removeListener(ServerPlayer player) {
        players.remove(player);
    }

    public void sendUpdate() {
        if (!hasChanged()) return;
        if (!players.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            CompoundTag items = new CompoundTag();
            CompoundTag components = new CompoundTag();
            CompoundTag nbtData = new CompoundTag();

            changedItems.forEach(itemId -> {
                items.putLong(itemId, storageItems.getOrDefault(itemId, 0L));

                // 发送完整组件快照（如果是带组件物品）
                CompoundTag snapshot = componentDataCache.get(itemId);
                if (snapshot != null) {
                    components.put(itemId, snapshot);
                } else if (nbtDataCache.get(itemId) instanceof CompoundTag compoundTag) {
                    // 旧版 CUSTOM_DATA 条目
                    nbtData.put(itemId, compoundTag);
                }
            });

            tag.put("items", items);
            tag.put("components", components);
            tag.put("nbtData", nbtData);

            players.forEach(player -> NetworkHandler.CHANNEL.sendTo(player, new S2CInfinityChestStatePack(ChannelState.COMMON, tag)));
        }
        resetChanged();
    }

    private boolean hasChanged() {
        return !changedItems.isEmpty();
    }

    private void resetChanged() {
        changedItems.clear();
    }

    public void sendFullUpdate() {
        if (!hasChanged()) return;
        if (!players.isEmpty()) {
            players.forEach(player -> NetworkHandler.CHANNEL.sendTo(player, new S2CInfinityChestStatePack(ChannelState.FULL, buildData())));
        }
        changedItems.clear();
    }

    public CompoundTag buildData() {
        CompoundTag items = new CompoundTag();
        CompoundTag components = new CompoundTag();
        CompoundTag nbtData = new CompoundTag();

        storageItems.forEach((itemId, count) -> {
            items.putLong(itemId, count);

            // 写入完整组件快照（如果是带组件物品）
            CompoundTag snapshot = componentDataCache.get(itemId);
            if (snapshot != null) {
                components.put(itemId, snapshot);
            } else if (nbtDataCache.get(itemId) instanceof CompoundTag compoundTag) {
                // 旧版 CUSTOM_DATA 条目（保持旧格式，避免兼容数据丢失）
                nbtData.put(itemId, compoundTag);
            }
        });

        CompoundTag data = new CompoundTag();
        data.put("items", items);
        data.put("components", components);
        data.put("nbtData", nbtData);
        return data;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved() {
        players.clear();
        this.removed = true;
    }
}
