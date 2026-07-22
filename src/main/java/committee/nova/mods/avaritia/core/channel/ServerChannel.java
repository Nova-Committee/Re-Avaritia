package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.common.net.channel.S2CChannelStatePack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Server-owned channel with persistence and viewer synchronization hooks. */
public class ServerChannel extends Channel {
    private static final int SYNC_PAGE_SIZE = 512;
    private final Set<ServerPlayer> listeners = new HashSet<>();
    private Runnable dirtyCallback = () -> { };
    private boolean removed;

    public ServerChannel() {
    }

    public ServerChannel(Data data) {
        super(data);
    }

    void attachDirtyCallback(Runnable callback) {
        dirtyCallback = callback == null ? () -> { } : callback;
    }

    public void addListener(ServerPlayer player) {
        if (!removed) {
            listeners.add(player);
            sendFull(player);
        }
    }

    public void removeListener(ServerPlayer player) {
        listeners.remove(player);
    }

    public void setRemoved() {
        removed = true;
        listeners.clear();
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    protected void onStorageCommitted(Snapshot originalState) {
        if (removed) return;
        dirtyCallback.run();
        List<ItemEntry> changedItems = new ArrayList<>();
        Set<net.neoforged.neoforge.transfer.item.ItemResource> itemKeys = new LinkedHashSet<>(originalState.items().keySet());
        itemKeys.addAll(itemEntries().stream().map(ItemEntry::resource).toList());
        for (var resource : itemKeys) {
            long before = originalState.items().getOrDefault(resource, 0L);
            long after = itemAmount(resource);
            if (before != after) changedItems.add(new ItemEntry(resource, after));
        }
        List<FluidEntry> changedFluids = new ArrayList<>();
        Set<net.neoforged.neoforge.transfer.fluid.FluidResource> fluidKeys = new LinkedHashSet<>(originalState.fluids().keySet());
        fluidKeys.addAll(fluidEntries().stream().map(FluidEntry::resource).toList());
        for (var resource : fluidKeys) {
            long before = originalState.fluids().getOrDefault(resource, 0L);
            long after = fluidAmount(resource);
            if (before != after) changedFluids.add(new FluidEntry(resource, after));
        }
        if (changedItems.size() + changedFluids.size() > SYNC_PAGE_SIZE) {
            sendFullToListeners();
        } else {
            broadcast(S2CChannelStatePack.delta(new Data(getName(), changedItems, changedFluids, energyAmount())));
        }
    }

    @Override
    protected void onMetadataChanged() {
        if (removed) return;
        dirtyCallback.run();
        broadcast(S2CChannelStatePack.delta(new Data(getName(), List.of(), List.of(), energyAmount())));
    }

    private void sendFull(ServerPlayer player) {
        Data snapshot = data();
        List<ItemEntry> items = snapshot.items();
        List<FluidEntry> fluids = snapshot.fluids();
        int itemIndex = 0;
        int fluidIndex = 0;
        boolean first = true;
        while (itemIndex < items.size() || fluidIndex < fluids.size() || first) {
            List<ItemEntry> itemPage = new ArrayList<>();
            List<FluidEntry> fluidPage = new ArrayList<>();
            int remaining = SYNC_PAGE_SIZE;
            while (itemIndex < items.size() && remaining-- > 0) itemPage.add(items.get(itemIndex++));
            while (fluidIndex < fluids.size() && remaining-- > 0) fluidPage.add(fluids.get(fluidIndex++));
            PacketDistributor.sendToPlayer(player, new S2CChannelStatePack(
                    first ? committee.nova.mods.avaritia.common.net.channel.ChannelState.FULL_START
                            : committee.nova.mods.avaritia.common.net.channel.ChannelState.FULL_PAGE,
                    new Data(snapshot.name(), itemPage, fluidPage, snapshot.energy())));
            first = false;
        }
        PacketDistributor.sendToPlayer(player, new S2CChannelStatePack(
                committee.nova.mods.avaritia.common.net.channel.ChannelState.FULL_END,
                new Data(snapshot.name(), List.of(), List.of(), snapshot.energy())));
    }

    private void broadcast(S2CChannelStatePack packet) {
        if (listeners.isEmpty()) return;
        listeners.removeIf(ServerPlayer::hasDisconnected);
        listeners.forEach(player -> PacketDistributor.sendToPlayer(player, packet));
    }

    private void sendFullToListeners() {
        if (listeners.isEmpty()) return;
        listeners.removeIf(ServerPlayer::hasDisconnected);
        listeners.forEach(this::sendFull);
    }
}
