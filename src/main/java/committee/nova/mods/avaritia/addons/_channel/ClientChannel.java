package committee.nova.mods.avaritia.addons._channel;

import committee.nova.mods.avaritia.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 03:21
 * @Description:
 */
public class ClientChannel extends Channel {

    @Nullable
    public DummyContainer container;

    public ClientChannel() {}

    public void addListener(DummyContainer container) {
        this.container = container;
    }

    public void removeListener() {
        this.container = null;
        storageItems.clear();
        storageFluids.clear();
        storageEnergies.clear();
    }

    @Override
    public void onItemChanged(Item itemId, boolean listChanged) {
        super.onItemChanged(itemId, listChanged);
        if (container != null) container.refreshContainer(listChanged);
    }

    @Override
    public void onFluidChanged(Fluid fluidId, boolean listChanged) {
        super.onFluidChanged(fluidId,listChanged);
        if (container != null) container.refreshContainer(listChanged);
    }

    @Override
    public void onEnergyChanged(String energyId, boolean listChanged) {
        if (container != null) container.refreshContainer(listChanged);
    }

    public void update(CompoundTag tag) {
        if (container == null) return;
        CompoundTag items = tag.getCompound("items");
        CompoundTag fluids = tag.getCompound("fluids");
        CompoundTag energies = tag.getCompound("energies");
        String name = tag.getString("name");
        AtomicBoolean fullUpdate = new AtomicBoolean(false);
        AtomicBoolean needRefreshContainer = new AtomicBoolean(false);
        items.getAllKeys().forEach(itemId -> {
            Item item = StorageUtils.getItem(itemId);
            long count = items.getLong(itemId);
            if (count <= 0L) {
                if (storageItems.containsKey(item)) {
                    storageItems.remove(item);
                    fullUpdate.set(true);
                    needRefreshContainer.set(true);
                }
            } else {
                if (storageItems.containsKey(item)) {
                    if (storageItems.get(item) != count) {
                        storageItems.replace(item, count);
                        needRefreshContainer.set(true);
                    }
                } else {
                    storageItems.put(item, count);
                    fullUpdate.set(true);
                    needRefreshContainer.set(true);
                }
            }
        });
        fluids.getAllKeys().forEach(fluidId -> {
            Fluid fluid = StorageUtils.getFluid(fluidId);
            long count = fluids.getLong(fluidId);
            if (count <= 0L ) {
                if (storageFluids.containsKey(fluid)) {
                    storageFluids.remove(fluid);
                    fullUpdate.set(true);
                    needRefreshContainer.set(true);
                }
            } else {
                if (storageFluids.containsKey(fluid)) {
                    if (storageFluids.get(fluid) != count) {
                        storageFluids.replace(fluid, count);
                        needRefreshContainer.set(true);
                    }
                } else {
                    storageFluids.put(fluid, count);
                    fullUpdate.set(true);
                    needRefreshContainer.set(true);
                }
            }
        });
        energies.getAllKeys().forEach(energyId -> {
            long count = energies.getLong(energyId);
            if (count <= 0L ) {
                if (storageEnergies.containsKey(energyId)) {
                    storageEnergies.remove(energyId);
                    fullUpdate.set(true);
                    needRefreshContainer.set(true);
                }
            } else {
                if (storageEnergies.containsKey(energyId)) {
                    if (storageEnergies.get(energyId) != count) {
                        storageEnergies.replace(energyId, count);
                        needRefreshContainer.set(true);
                    }
                } else {
                    storageEnergies.put(energyId, count);
                    fullUpdate.set(true);
                    needRefreshContainer.set(true);
                }
            }
        });
        if (!name.isEmpty()) setName(name);
        if (needRefreshContainer.get()) container.refreshContainer(fullUpdate.get());
        if (fullUpdate.get()) {
            updateItemKeys();
            updateFluidKeys();
        }
    }

    public void fullUpdate(CompoundTag tag) {
        CompoundTag items = tag.getCompound("items");
        CompoundTag fluids = tag.getCompound("fluids");
        CompoundTag energies = tag.getCompound("energies");
        String name = tag.getString("name");
        storageItems.clear();
        storageFluids.clear();
        storageEnergies.clear();
        items.getAllKeys().forEach(itemId -> storageItems.put(StorageUtils.getItem(itemId), items.getLong(itemId)));
        fluids.getAllKeys().forEach(fluidId -> storageFluids.put(StorageUtils.getFluid(fluidId), fluids.getLong(fluidId)));
        energies.getAllKeys().forEach(energyId -> storageEnergies.put(energyId, energies.getLong(energyId)));
        updateItemKeys();
        updateFluidKeys();
        setName(name);
        if (container != null) {
            container.refreshContainer(true);
        }
    }

    @Override
    public boolean isRemoved() {
        return false;
    }
}
