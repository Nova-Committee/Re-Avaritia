package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.common.net.channel.ChannelState;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
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
        if (dat.contains("items")) {
            CompoundTag items = dat.getCompound("items");
            items.getAllKeys().forEach(itemId -> {
                if (items.getLong(itemId) > 0 && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemId))) {
                    storageItems.put(itemId, items.getLong(itemId));
                }
            });
            updateItemKeys();
        }
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
            changedItems.forEach(itemId -> items.putLong(itemId, storageItems.getOrDefault(itemId, 0L)));
            tag.put("items", items);
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
        storageItems.forEach(items::putLong);
        CompoundTag data = new CompoundTag();
        data.put("items", items);
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
