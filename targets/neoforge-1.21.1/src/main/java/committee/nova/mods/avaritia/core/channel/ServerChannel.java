package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.common.net.channel.ChannelState;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelStatePack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;

/** A single server-owned Tesseract channel. */
public class ServerChannel extends Channel {
    private final Set<String> changedItems = new HashSet<>();
    private final Set<String> changedFluids = new HashSet<>();
    private final Set<String> changedEnergy = new HashSet<>();
    private final Set<ServerPlayer> players = new HashSet<>();
    private boolean nameChanged;
    private boolean removed;
    private boolean loadComplete = true;

    public ServerChannel() {
    }

    public ServerChannel(String name) {
        setName(name);
    }

    public ServerChannel(CompoundTag data) {
        initialize(data);
    }

    @Override
    public void onItemChanged(String itemId, boolean listChanged) {
        super.onItemChanged(itemId, listChanged);
        changedItems.add(itemId);
    }

    @Override
    public void onFluidChanged(String fluidId, boolean listChanged) {
        super.onFluidChanged(fluidId, listChanged);
        changedFluids.add(fluidId);
    }

    @Override
    public void onEnergyChanged(String energyId, boolean listChanged) {
        changedEnergy.add(energyId);
    }

    public final void initialize(CompoundTag data) {
        if (data.contains("name")) {
            super.setName(data.getString("name"));
        }
        loadComplete = true;
        storageItems.clear();
        loadComplete &= readEntries(data.getCompound("items"), storageItems, BuiltInRegistries.ITEM::containsKey);
        updateItemKeys();
        storageFluids.clear();
        loadComplete &= readEntries(data.getCompound("fluids"), storageFluids, BuiltInRegistries.FLUID::containsKey);
        updateFluidKeys();
        storageEnergies.clear();
        CompoundTag energies = data.getCompound("energies");
        energies.getAllKeys().forEach(id -> {
            long amount = energies.getLong(id);
            if (amount > 0) {
                storageEnergies.put(id, amount);
            }
        });
        resetChanged();
    }

    private static boolean readEntries(CompoundTag tag, java.util.Map<String, Long> target,
                                       java.util.function.Predicate<ResourceLocation> registryCheck) {
        boolean complete = true;
        for (String id : tag.getAllKeys()) {
            ResourceLocation location = ResourceLocation.tryParse(id);
            long amount = tag.getLong(id);
            if (amount <= 0) {
                continue;
            }
            if (location != null && registryCheck.test(location)) {
                target.put(id, amount);
            } else {
                complete = false;
            }
        }
        return complete;
    }

    public boolean isLoadComplete() {
        return loadComplete;
    }

    public void addListener(ServerPlayer player) {
        players.add(player);
        PacketDistributor.sendToPlayer(player, new S2CChannelStatePack(ChannelState.FULL, buildData()));
    }

    public void removeListener(ServerPlayer player) {
        players.remove(player);
    }

    public void sendUpdate() {
        if (!hasChanged()) {
            return;
        }
        if (!players.isEmpty()) {
            CompoundTag data = new CompoundTag();
            data.put("items", changedValues(changedItems, storageItems));
            data.put("fluids", changedValues(changedFluids, storageFluids));
            data.put("energies", changedValues(changedEnergy, storageEnergies));
            if (nameChanged) {
                data.putString("name", getName());
            }
            S2CChannelStatePack payload = new S2CChannelStatePack(ChannelState.COMMON, data);
            players.removeIf(player -> player.hasDisconnected());
            players.forEach(player -> PacketDistributor.sendToPlayer(player, payload));
        }
        resetChanged();
    }

    private static CompoundTag changedValues(Set<String> changed, java.util.Map<String, Long> values) {
        CompoundTag result = new CompoundTag();
        changed.forEach(id -> result.putLong(id, values.getOrDefault(id, 0L)));
        return result;
    }

    private boolean hasChanged() {
        return !changedItems.isEmpty() || !changedFluids.isEmpty() || !changedEnergy.isEmpty() || nameChanged;
    }

    private void resetChanged() {
        changedItems.clear();
        changedFluids.clear();
        changedEnergy.clear();
        nameChanged = false;
    }

    public void sendFullUpdate() {
        if (!players.isEmpty()) {
            S2CChannelStatePack payload = new S2CChannelStatePack(ChannelState.FULL, buildData());
            players.removeIf(ServerPlayer::hasDisconnected);
            players.forEach(player -> PacketDistributor.sendToPlayer(player, payload));
        }
        resetChanged();
    }

    @Override
    public void setName(String channelName) {
        nameChanged = true;
        super.setName(channelName);
    }

    public CompoundTag buildData() {
        CompoundTag data = new CompoundTag();
        data.putString("name", getName());
        data.put("items", writeEntries(storageItems));
        data.put("fluids", writeEntries(storageFluids));
        data.put("energies", writeEntries(storageEnergies));
        return data;
    }

    private static CompoundTag writeEntries(java.util.Map<String, Long> entries) {
        CompoundTag result = new CompoundTag();
        entries.forEach(result::putLong);
        return result;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved() {
        players.clear();
        removed = true;
    }
}
