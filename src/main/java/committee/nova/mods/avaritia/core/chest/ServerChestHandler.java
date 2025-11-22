package committee.nova.mods.avaritia.core.chest;

import committee.nova.mods.avaritia.common.net.chest.ChannelState;
import committee.nova.mods.avaritia.common.net.chest.S2CInfinityChestStatePack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashSet;
import java.util.List;

/**
 * @author cnlimiter
 */
public class ServerChestHandler extends ChestHandler {
    private final HashSet<ItemSuper> changedItems = new HashSet<>();
    private final HashSet<ServerPlayer> players = new HashSet<>();
    private boolean removed = false;
    private MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

    public ServerChestHandler() {}

    public ServerChestHandler(MinecraftServer server, CompoundTag dat) {
        this.server = server;
        initialize(dat);
    }

    @Override
    public void onItemChanged(ItemSuper itemId, boolean listChanged) {
        super.onItemChanged(itemId, listChanged);
        changedItems.add(itemId);
    }

    public void initialize(CompoundTag dat) {
        storageItems.clear();

        if (dat.contains("items")) {
            var items = dat.getList("items", Tag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); ++i) {
                final CompoundTag item = items.getCompound(i);
                var itemSuper = ItemSuper.fromTag(this.server.registryAccess(), item);
                if (itemSuper != null) storageItems.put(itemSuper, itemSuper.getRealCount());
            }
        }
        updateItemKeys();
    }

    public void addListener(ServerPlayer player) {
        players.add(player);
        PacketDistributor.sendToAllPlayers(new S2CInfinityChestStatePack(ChannelState.FULL, this.storageItems.keySet()));
    }

    public void removeListener(ServerPlayer player) {
        players.remove(player);
    }

    public void sendUpdate() {
        if (!hasChanged()) return;
        if (!players.isEmpty()) {
            List<ItemSuper> changed = changedItems.stream().toList();
            players.forEach(player -> PacketDistributor.sendToPlayer(player, new S2CInfinityChestStatePack(ChannelState.COMMON, changed)));
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
            players.forEach(player -> PacketDistributor.sendToPlayer(player, new S2CInfinityChestStatePack(ChannelState.FULL, storageItems.keySet())));
        }
        changedItems.clear();
    }

    public CompoundTag buildData() {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();

        storageItems.forEach((itemSuper, count) -> {
            items.add(itemSuper.toTag(this.server.registryAccess()));
        });

        tag.put("items", items);
        return tag;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved() {
        players.clear();
        this.removed = true;
    }
}
