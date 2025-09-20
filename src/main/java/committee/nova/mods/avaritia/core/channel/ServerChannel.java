package committee.nova.mods.avaritia.core.channel;

import committee.nova.mods.avaritia.common.net.channel.ChannelState;
import committee.nova.mods.avaritia.common.net.channel.S2CChannelStatePack;
import committee.nova.mods.avaritia.init.handler.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 03:13
 * @Description:
 */
public class ServerChannel extends Channel {

    private final HashSet<String> changedItems = new HashSet<>();
    private final HashSet<String> changedFluids = new HashSet<>();
    private final HashSet<String> changedEnergy = new HashSet<>();
    private boolean nameChanged = false;
    private final HashSet<ServerPlayer> players = new HashSet<>();
    private boolean removed = false;


    public ServerChannel() {
    }

    public ServerChannel(String name) {
        this.setName(name);
    }

    public ServerChannel(CompoundTag dat) {
        initialize(dat);
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

    public void initialize(CompoundTag dat) {
        if (dat.contains("name")) this.setName(dat.getString("name"));
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
        storageFluids.clear();
        if (dat.contains("fluids")) {
            CompoundTag fluids = dat.getCompound("fluids");
            fluids.getAllKeys().forEach(fluidId -> {
                if (fluids.getLong(fluidId) > 0 && ForgeRegistries.FLUIDS.containsKey(new ResourceLocation(fluidId))) {
                    storageFluids.put(fluidId, fluids.getLong(fluidId));
                }
            });
            updateFluidKeys();
        }
        storageEnergies.clear();
        if (dat.contains("energies")) {
            CompoundTag energies = dat.getCompound("energies");
            energies.getAllKeys().forEach(energy -> {
                if (energies.getLong(energy) > 0 && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(energy))) {
                    storageEnergies.put(energy, energies.getLong(energy));
                }
            });
        }
    }

    public void addListener(ServerPlayer player) {
        players.add(player);
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CChannelStatePack(ChannelState.FULL, buildData()));
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

            CompoundTag fluids = new CompoundTag();
            changedFluids.forEach(fluidId -> fluids.putLong(fluidId, storageFluids.getOrDefault(fluidId, 0L)));
            tag.put("fluids", fluids);

            CompoundTag energies = new CompoundTag();
            changedEnergy.forEach(energyId -> energies.putLong(energyId, storageEnergies.getOrDefault(energyId, 0L)));
            tag.put("energies", energies);

            if (nameChanged) tag.putString("name", getName());

            players.forEach(player -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CChannelStatePack(ChannelState.COMMON, tag)));
        }
        resetChanged();
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
        if (!hasChanged()) return;
        if (!players.isEmpty()) {
            players.forEach(player -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CChannelStatePack(ChannelState.FULL, buildData())));
        }
        changedItems.clear();
    }

    @Override
    public void setName(String channelName) {
        nameChanged = true;
        super.setName(channelName);
    }

    public CompoundTag buildData() {
        CompoundTag items = new CompoundTag();
        storageItems.forEach(items::putLong);
        CompoundTag fluids = new CompoundTag();
        storageFluids.forEach(fluids::putLong);
        CompoundTag energies = new CompoundTag();
        storageEnergies.forEach(energies::putLong);
        CompoundTag data = new CompoundTag();
        data.putString("name", getName());
        data.put("items", items);
        data.put("fluids", fluids);
        data.put("energies", energies);
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
