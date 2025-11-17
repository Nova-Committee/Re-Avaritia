package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.common.net.channel.ChannelState;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

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
        nbtDataCache.clear();

        if (dat.contains("items")) {
            CompoundTag items = dat.getCompound("items");
            items.getAllKeys().forEach(itemId -> {
                if (items.getLong(itemId) > 0 && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(StorageUtils.getBaseItemId(itemId)))) {
                    storageItems.put(itemId, items.getLong(itemId));
                }
            });
        }

        // 加载NBT数据（如果有）
        if (dat.contains("nbtData")) {
            CompoundTag nbtData = dat.getCompound("nbtData");
            nbtData.getAllKeys().forEach(itemId -> {
                Tag nbtTag = nbtData.get(itemId);
                if (nbtTag instanceof CompoundTag compoundTag) {
                    nbtDataCache.put(itemId, compoundTag);
                }
            });
        }

        updateItemKeys();
    }

    public void addListener(ServerPlayer player) {
        players.add(player);
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CInfinityChestStatePack(ChannelState.FULL, buildData()));
    }

    public void removeListener(ServerPlayer player) {
        players.remove(player);
    }

    public void sendUpdate() {
        if (!hasChanged()) return;
        if (!players.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            CompoundTag items = new CompoundTag();
            CompoundTag nbtData = new CompoundTag();

            changedItems.forEach(itemId -> {
                items.putLong(itemId, storageItems.getOrDefault(itemId, 0L));

                // 发送NBT数据（如果是NBT物品）
                if (itemId.contains("#") && nbtDataCache.containsKey(itemId)) {
                    Tag nbtTag = nbtDataCache.get(itemId);
                    if (nbtTag instanceof CompoundTag compoundTag) {
                        nbtData.put(itemId, compoundTag);
                    }
                }
            });

            tag.put("items", items);
            tag.put("nbtData", nbtData);

            players.forEach(player -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CInfinityChestStatePack(ChannelState.COMMON, tag)));
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
            players.forEach(player -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CInfinityChestStatePack(ChannelState.FULL, buildData())));
        }
        changedItems.clear();
    }

    public CompoundTag buildData() {
        CompoundTag items = new CompoundTag();
        CompoundTag nbtData = new CompoundTag();

        storageItems.forEach((itemId, count) -> {
            items.putLong(itemId, count);

            // 发送NBT数据（如果是NBT物品）
            if (itemId.contains("#") && nbtDataCache.containsKey(itemId)) {
                Tag nbtTag = nbtDataCache.get(itemId);
                if (nbtTag instanceof CompoundTag compoundTag) {
                    nbtData.put(itemId, compoundTag);
                }
            }
        });

        CompoundTag data = new CompoundTag();
        data.put("items", items);
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
