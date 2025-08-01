package committee.nova.mods.avaritia.addons.channel;

import committee.nova.mods.avaritia.util.StorageUtils;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/2/28 02:59
 * @Description:
 */
public abstract class Channel implements IItemHandler, IFluidHandler, IEnergyStorage {
    private String channelName = "UnName";
    //TODO:兼容mek气体和其他能量
    public final HashMap<String, Long> storageItems = new HashMap<>();
    public final HashMap<String, Long> storageFluids = new HashMap<>();
    public final HashMap<String, Long> storageEnergies = new HashMap<>();
    private String[] itemKeys = new String[]{};
    private String[] fluidKeys = new String[]{};
    private ItemStack[] slotItemTemp = {ItemStack.EMPTY};
    private FluidStack[] slotFluidTemp = {FluidStack.EMPTY};
    public final int maxChannelSize = 2000;

    public Channel() {}

    public void onItemChanged(String itemId, boolean listChanged) {
        if (listChanged) updateItemKeys();
    }

    public void onFluidChanged(String fluidId, boolean listChanged) {
        if (listChanged) updateFluidKeys();
    }

    public abstract void onEnergyChanged(String energyId, boolean listChanged);

    public void updateItemKeys() {
        itemKeys = storageItems.keySet().toArray(new String[]{});
        slotItemTemp = new ItemStack[itemKeys.length];
        for (int i = 0; i < itemKeys.length; i++) slotItemTemp[i] = new ItemStack(StorageUtils.getItem(itemKeys[i]));
    }

    public void updateFluidKeys() {
        fluidKeys = storageFluids.keySet().toArray(new String[]{});
        slotFluidTemp = new FluidStack[fluidKeys.length];
        for (int i = 0; i < fluidKeys.length; i++) slotFluidTemp[i] = new FluidStack(StorageUtils.getFluid(fluidKeys[i]), 1);
    }

    public int getChannelSize() {
        return storageItems.size() + storageFluids.size() + storageEnergies.size();
    }

    public boolean hasItem(String item) { return storageItems.containsKey(item); }

    public int getItemAmount(String item) {
        return (int) Long.min(Integer.MAX_VALUE, storageItems.getOrDefault(item, 0L));
    }

    public long getRealItemAmount(String item) {
        return storageItems.getOrDefault(item, 0L);
    }

    public int getFluidAmount(String fluid) {
        return (int) Long.min(Integer.MAX_VALUE, storageFluids.getOrDefault(fluid, 0L));
    }

    public long getRealFluidAmount(String fluid) {
        return storageFluids.getOrDefault(fluid, 0L);
    }

    public int getFEAmount() {
        return (int) Long.min(Integer.MAX_VALUE, storageEnergies.getOrDefault("avaritia:forge_energy", 0L));
    }

    public long getRealFEAmount() {
        return storageEnergies.getOrDefault("avaritia:forge_energy", 0L);
    }

    public int getStorageEnergy(String energyId) {
        return (int) Long.min(Integer.MAX_VALUE, storageEnergies.getOrDefault(energyId, 0L));
    }

    public long getRealEnergyAmount(String energyId) {
        return storageEnergies.getOrDefault(energyId, 0L);
    }

    public int getStorageAmount(Item item) {
        return (int) Long.min(Integer.MAX_VALUE, storageItems.getOrDefault(StorageUtils.getItemId(item), 0L));
    }

    public int getStorageAmount(Fluid fluid) {
        return (int) Long.min(Integer.MAX_VALUE, storageFluids.getOrDefault(StorageUtils.getFluidId(fluid), 0L));
    }

    public int canStorageAmount(ItemStack itemStack) {
        if (itemStack.hasTag()) return 0;
        long a = storageItems.getOrDefault(StorageUtils.getItemId(itemStack.getItem()), 0L);
        if (a == 0L) {
            if (getChannelSize() >= maxChannelSize) return 0;
            else return Integer.MAX_VALUE;
        }
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    public int canStorageAmount(FluidStack fluidStack) {
        if (fluidStack.hasTag()) return 0;
        long a = storageFluids.getOrDefault(StorageUtils.getFluidId(fluidStack.getFluid()), 0L);
        if (a == 0L) {
            if (getChannelSize() >= maxChannelSize) return 0;
            else return Integer.MAX_VALUE;
        }
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    public long canStorageRealAmount(FluidStack fluidStack) {
        if (fluidStack.hasTag()) return 0;
        long a = storageFluids.getOrDefault(StorageUtils.getFluidId(fluidStack.getFluid()), 0L);
        if (a == 0L) {
            if (getChannelSize() >= maxChannelSize) return 0;
            else return Long.MAX_VALUE;
        }
        return Long.MAX_VALUE - a;
    }

    public boolean canStorageItem(String item) {
        if (storageItems.containsKey(item)) {
            return storageItems.get(item) < Long.MAX_VALUE;
        } else return getChannelSize() < maxChannelSize;
    }

    public int canStorageItemAmount(String item) {
        long a = storageItems.getOrDefault(item, 0L);
        if (a == 0L) {
            if (getChannelSize() >= maxChannelSize) return 0;
            else return Integer.MAX_VALUE;
        }
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    public int canStorageFluidAmount(String fluidId) {
        long a = storageFluids.getOrDefault(fluidId, 0L);
        if (a == 0L) {
            if (getChannelSize() >= maxChannelSize) return 0;
            else return Integer.MAX_VALUE;
        }
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    public boolean canStorageFE() {
        if (storageEnergies.containsKey("avaritia:forge_energy")) {
            return storageEnergies.get("avaritia:forge_energy") < Long.MAX_VALUE;
        } else return getChannelSize() < maxChannelSize;
    }

    public int canStorageFEAmount() {
        long a = storageEnergies.getOrDefault("avaritia:forge_energy", 0L);
        if (a == 0L) {
            if (getChannelSize() >= maxChannelSize) return 0;
            else return Integer.MAX_VALUE;
        }
        return (int) Math.min(Integer.MAX_VALUE, Long.MAX_VALUE - a);
    }

    public String[] getItemKeys() {
        return itemKeys;
    }

    public String[] getFluidKeys() {
        return fluidKeys;
    }

    /**
     * @param itemStack 会被修改，塞不进去会有余，
     * @return 存进去的量
     */
    public int addItem(ItemStack itemStack) {
        if (itemStack.hasTag() || itemStack.isEmpty()) return 0;
        String itemId = StorageUtils.getItemId(itemStack.getItem());
        int count = itemStack.getCount();
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= itemStack.getCount()) {
                storageItems.replace(itemId, storageCount + itemStack.getCount());
                itemStack.setCount(0);
                onItemChanged(itemId, false);
                return count;
            } else {
                storageItems.replace(itemId, Long.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - (int) remainingSpaces);
                onItemChanged(itemId, false);
                return (int) remainingSpaces;
            }
        } else {
            if (getChannelSize() >= maxChannelSize) return 0;
            storageItems.put(itemId, (long) itemStack.getCount());
            itemStack.setCount(0);
            onItemChanged(itemId, true);
            return count;
        }
    }

    /**
     * @param fluidStack 会被修改，塞不进去会有余，
     * @return 存进去的量
     */
    public int addFluid(FluidStack fluidStack) {
        if (fluidStack.hasTag() || fluidStack.isEmpty()) return 0;
        String fluidId = StorageUtils.getFluidId(fluidStack.getFluid());
        int count = fluidStack.getAmount();
        if (storageFluids.containsKey(fluidId)) {
            long storageAmount = storageFluids.get(fluidId);
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= fluidStack.getAmount()) {
                storageFluids.replace(fluidId, storageAmount + fluidStack.getAmount());
                fluidStack.setAmount(0);
                onFluidChanged(fluidId, false);
                return count;
            } else {
                storageFluids.replace(fluidId, Long.MAX_VALUE);
                fluidStack.setAmount(fluidStack.getAmount() - (int) remainingSpaces);
                onFluidChanged(fluidId, false);
                return (int) remainingSpaces;
            }
        } else {
            if (getChannelSize() >= maxChannelSize) return 0;
            storageFluids.put(fluidId, (long) fluidStack.getAmount());
            fluidStack.setAmount(0);
            onFluidChanged(fluidId, true);
            return count;
        }
    }

    /**
     * @return 成功进入的
     */
    public long addItem(String itemId, long count) {
        if (itemId.equals("minecraft:air") || count == 0) return 0L;
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= count) {
                storageItems.replace(itemId, storageCount + count);
                onItemChanged(itemId, false);
                return count;
            } else {
                storageItems.replace(itemId, Long.MAX_VALUE);
                onItemChanged(itemId, false);
                return remainingSpaces;
            }
        } else {
            if (getChannelSize() >= maxChannelSize) return 0L;
            storageItems.put(itemId, count);
            onItemChanged(itemId, true);
            return count;
        }
    }

    /**
     * @return 成功进入的
     */
    public long addFluid(String fluidId, long count) {
        if (fluidId.equals("minecraft:air") || count == 0) return 0L;
        if (storageFluids.containsKey(fluidId)) {
            long storageAmount = storageFluids.get(fluidId);
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= count) {
                storageFluids.replace(fluidId, storageAmount + count);
                onFluidChanged(fluidId, false);
                return count;
            } else {
                storageFluids.replace(fluidId, Long.MAX_VALUE);
                onFluidChanged(fluidId, false);
                return remainingSpaces;
            }
        } else {
            if (getChannelSize() >= maxChannelSize) return 0L;
            storageFluids.put(fluidId, count);
            onFluidChanged(fluidId, true);
            return count;
        }
    }


    /**
     * @return 成功进入的
     */
    public int addEnergy(int count) {
        if (storageEnergies.containsKey("avaritia:forge_energy")) {
            long storageAmount = storageEnergies.get("avaritia:forge_energy");
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= count) {
                storageEnergies.replace("avaritia:forge_energy", storageAmount + count);
                onEnergyChanged("avaritia:forge_energy", false);
                return count;
            } else {
                storageEnergies.replace("avaritia:forge_energy", Long.MAX_VALUE);
                onEnergyChanged("avaritia:forge_energy", false);
                return (int) remainingSpaces;
            }
        } else {
            if (getChannelSize() >= maxChannelSize) return 0;
            storageEnergies.put("avaritia:forge_energy", (long) count);
            onEnergyChanged("avaritia:forge_energy", true);
            return count;
        }
    }

    /**
     * @return 成功进入的
     */
    public long addEnergy(long count) {
        if (storageEnergies.containsKey("avaritia:forge_energy")) {
            long storageAmount = storageEnergies.get("avaritia:forge_energy");
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= count) {
                storageEnergies.replace("avaritia:forge_energy", storageAmount + count);
                onEnergyChanged("avaritia:forge_energy", false);
                return count;
            } else {
                storageEnergies.replace("avaritia:forge_energy", Long.MAX_VALUE);
                onEnergyChanged("avaritia:forge_energy", false);
                return remainingSpaces;
            }
        } else {
            if (getChannelSize() >= maxChannelSize) return 0L;
            storageEnergies.put("avaritia:forge_energy", count);
            onEnergyChanged("avaritia:forge_energy", true);
            return count;
        }
    }

    /**
     * @return 成功进入的
     */
    public long addEnergy(String energyId, long count) {
        if (storageEnergies.containsKey(energyId)) {
            long storageAmount = storageEnergies.get(energyId);
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= count) {
                storageEnergies.replace(energyId, storageAmount + count);
                onEnergyChanged(energyId, false);
                return count;
            } else {
                storageEnergies.replace(energyId, Long.MAX_VALUE);
                onEnergyChanged(energyId, false);
                return remainingSpaces;
            }
        } else {
            if (getChannelSize() >= maxChannelSize) return 0L;
            storageEnergies.put(energyId, count);
            onEnergyChanged(energyId, true);
            return count;
        }
    }

    /**
     * 填充物品叠堆，不限制数量。
     *
     * @param itemStack 要填充的物品
     * @param count     要填充的数量，负数为扣除。
     */
    public void fillItemStack(ItemStack itemStack, int count) {
        if (itemStack.isEmpty() || count == 0 || itemStack.hasTag()) return;
        String itemId = StorageUtils.getItemId(itemStack.getItem());
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (count >= storageCount) {
                storageItems.remove(itemId);
                itemStack.setCount(itemStack.getCount() + (int) storageCount);
                onItemChanged(itemId, true);
            } else if (remainingSpaces < -count) {
                storageItems.replace(itemId, Long.MAX_VALUE);
                itemStack.setCount(itemStack.getCount() - (int) remainingSpaces);
                onItemChanged(itemId, false);
            } else {
                storageItems.replace(itemId, storageCount - count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(itemId, false);
            }
        } else {
            if (count < 0) {
                if (getChannelSize() >= maxChannelSize) return;
                storageItems.put(itemId, (long) -count);
                itemStack.setCount(itemStack.getCount() + count);
                onItemChanged(itemId, true);
            }
        }
    }

    public void fillFluidStack(FluidStack fluidStack, int count) {
        if (fluidStack.isEmpty() || count == 0 || fluidStack.hasTag()) return;
        String fluidId = StorageUtils.getFluidId(fluidStack.getFluid());
        if (storageFluids.containsKey(fluidId)) {
            long storageCount = storageFluids.get(fluidId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (count >= storageCount) {
                storageFluids.remove(fluidId);
                fluidStack.setAmount(fluidStack.getAmount() + (int) storageCount);
                onFluidChanged(fluidId, true);
            } else if (remainingSpaces < -count) {
                storageFluids.replace(fluidId, Long.MAX_VALUE);
                fluidStack.setAmount(fluidStack.getAmount() - (int) remainingSpaces);
                onFluidChanged(fluidId, false);
            } else {
                storageFluids.replace(fluidId, storageCount - count);
                fluidStack.setAmount(fluidStack.getAmount() + count);
                onFluidChanged(fluidId, false);
            }
        } else {
            if (count < 0) {
                if (getChannelSize() >= maxChannelSize) return;
                storageFluids.put(fluidId, (long) -count);
                fluidStack.setAmount(fluidStack.getAmount() + count);
                onFluidChanged(fluidId, true);
            }
        }
    }

    /**
     * 从频道获取物品，但不限制数量。
     */
    public ItemStack takeItem(String itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.equals("minecraft:air") || count == 0) return ItemStack.EMPTY;
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = (int) storageCount;
            onItemChanged(itemId, true);
        }
        return new ItemStack(StorageUtils.getItem(itemId), count);
    }

    public FluidStack takeFluid(String fluidId, int count) {
        if (!storageFluids.containsKey(fluidId) || fluidId.equals("minecraft:air") || count == 0) return FluidStack.EMPTY;
        long storageAmount = storageFluids.get(fluidId);
        if (count < storageAmount) {
            storageFluids.replace(fluidId, storageAmount - count);
            onFluidChanged(fluidId, false);
        } else {
            storageFluids.remove(fluidId);
            count = (int) storageAmount;
            onFluidChanged(fluidId, true);
        }
        return new FluidStack(StorageUtils.getFluid(fluidId), count);
    }

    /**
     * 从频道获取物品，数量限制在叠堆最大值。
     */
    public ItemStack saveTakeItem(String itemId, int count) {
        if (!storageItems.containsKey(itemId) || itemId.equals("minecraft:air") || count == 0) return ItemStack.EMPTY;
        ItemStack itemStack = new ItemStack(StorageUtils.getItem(itemId), 1);
        count = Integer.min(count, itemStack.getMaxStackSize());
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = (int) storageCount;
            onItemChanged(itemId, true);
        }
        itemStack.setCount(count);
        return itemStack;
    }

    public ItemStack saveTakeItem(String itemId, boolean half) {
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
        ItemStack itemStack = new ItemStack(StorageUtils.getItem(itemId), 1);
        int count = half ? (itemStack.getMaxStackSize() + 1) / 2 : itemStack.getMaxStackSize();
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            count = (int) storageCount;
            onItemChanged(itemId, true);
        }
        itemStack.setCount(count);
        return itemStack;
    }

    public void removeItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) return;
        String itemId = StorageUtils.getItemId(itemStack.getItem());
        if (!storageItems.containsKey(itemId)) return;
        long storageCount = storageItems.get(itemId);
        if (itemStack.getCount() < storageCount) {
            storageItems.replace(itemId, storageCount - itemStack.getCount());
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            onItemChanged(itemId, true);
        }
    }

    public void removeItem(String itemId, long count) {
        if (!storageItems.containsKey(itemId)) return;
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            storageItems.replace(itemId, storageCount - count);
            onItemChanged(itemId, false);
        } else {
            storageItems.remove(itemId);
            onItemChanged(itemId, true);
        }
    }

    public void removeEnergy(Long amount) {
        if (!storageEnergies.containsKey("avaritia:forge_energy")) return;
        long storageCount = storageEnergies.get("avaritia:forge_energy");
        if (amount < storageCount) {
            storageEnergies.replace("avaritia:forge_energy", storageCount - amount);
            onEnergyChanged("avaritia:forge_energy", false);
        } else {
            storageEnergies.remove("avaritia:forge_energy");
            onEnergyChanged("avaritia:forge_energy", true);
        }
    }

    public void removeEnergy(String energyId, Long amount) {
        if (!storageEnergies.containsKey(energyId)) return;
        long storageCount = storageEnergies.get(energyId);
        if (amount < storageCount) {
            storageEnergies.replace(energyId, storageCount - amount);
            onEnergyChanged(energyId, false);
        } else {
            storageEnergies.remove(energyId);
            onEnergyChanged(energyId, true);
        }
    }

    public boolean isEmpty() {
        return storageItems.isEmpty() && storageFluids.isEmpty() && storageEnergies.isEmpty();
    }

    public abstract boolean isRemoved();

    public String getName() {
        return channelName;
    }

    public void setName(String channelName) {
        this.channelName = channelName.substring(0, Math.min(channelName.length(), 64));
    }


    @Override
    public int getSlots() {
        return storageItems.size() + 54;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot >= itemKeys.length + 27 || slot < 27) return ItemStack.EMPTY;
        ItemStack itemStack = slotItemTemp[slot - 27];
        itemStack.setCount((int) Math.min(Integer.MAX_VALUE, storageItems.get(itemKeys[slot - 27])));
        return itemStack;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || stack.hasTag()) return stack;
        String itemId = StorageUtils.getItemId(stack.getItem());
        ItemStack remainingStack = ItemStack.EMPTY;
        if (storageItems.containsKey(itemId)) {
            long storageCount = storageItems.get(itemId);
            long remainingSpaces = Long.MAX_VALUE - storageCount;
            if (remainingSpaces >= stack.getCount()) {
                if (!simulate) storageItems.replace(itemId, storageCount + stack.getCount());
            } else {
                if (!simulate) storageItems.replace(itemId, Long.MAX_VALUE);
                remainingStack = stack.copy();
                remainingStack.setCount(stack.getCount() - (int) remainingSpaces);
            }
            if (!simulate) onItemChanged(itemId, false);
        } else {
            if (getChannelSize() >= maxChannelSize) return stack;
            if (!simulate) {
                storageItems.put(itemId, (long) stack.getCount());
                onItemChanged(itemId, true);
            }
        }
        return remainingStack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= itemKeys.length + 27 || slot < 27) return ItemStack.EMPTY;
        String itemId = itemKeys[slot - 27];
        if (!storageItems.containsKey(itemId)) return ItemStack.EMPTY;
        ItemStack itemStack = new ItemStack(StorageUtils.getItem(itemId), 1);
        int count = Math.min(itemStack.getMaxStackSize(), amount);
        long storageCount = storageItems.get(itemId);
        if (count < storageCount) {
            if (!simulate) {
                storageItems.replace(itemId, storageCount - count);
                onItemChanged(itemId, false);
            }
        } else {
            if (!simulate) {
                storageItems.remove(itemId);
                onItemChanged(itemId, true);
            }
            count = (int) storageCount;
        }
        itemStack.setCount(count);
        return itemStack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return !stack.isEmpty() && !stack.hasTag();
    }


    @Override
    public int getTanks() {
        return storageFluids.size() + 18;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (tank >= fluidKeys.length + 9 || tank < 9) return FluidStack.EMPTY;
        FluidStack fluidStack = slotFluidTemp[tank - 9];
        fluidStack.setAmount((int) Math.min(Integer.MAX_VALUE, storageFluids.get(fluidKeys[tank - 9])));
        return fluidStack;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return !stack.isEmpty() && !stack.hasTag();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || resource.hasTag()) return 0;
        String fluidId = StorageUtils.getFluidId(resource.getFluid());
        if (storageFluids.containsKey(fluidId)) {
            long storageAmount = storageFluids.get(fluidId);
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= resource.getAmount()) {
                if (action == FluidAction.EXECUTE) {
                    storageFluids.replace(fluidId, storageAmount + resource.getAmount());
                    onFluidChanged(fluidId, false);
                }
                return resource.getAmount();
            } else {
                if (action == FluidAction.EXECUTE) {
                    storageFluids.replace(fluidId, Long.MAX_VALUE);
                    onFluidChanged(fluidId, false);
                }
                return (int) remainingSpaces;
            }
        } else {
            if (getChannelSize() >= maxChannelSize) return 0;
            if (action == FluidAction.EXECUTE) {
                storageFluids.put(fluidId, (long) resource.getAmount());
                onFluidChanged(fluidId, true);
            }
            return resource.getAmount();
        }
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        String fluidId = StorageUtils.getFluidId(resource.getFluid());
        if (!storageFluids.containsKey(fluidId) || resource.getAmount() <= 0) return FluidStack.EMPTY;
        long storageAmount = storageFluids.get(fluidId);
        int count = resource.getAmount();
        if (count < storageAmount) {
            if (action == FluidAction.EXECUTE) {
                storageFluids.replace(fluidId, storageAmount - count);
                onFluidChanged(fluidId, false);
            }
        } else {
            if (action == FluidAction.EXECUTE) {
                storageFluids.remove(fluidId);
                onFluidChanged(fluidId, true);
            }
            count = (int) storageAmount;
        }
        return new FluidStack(resource, count);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (storageFluids.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
        String fluidId = fluidKeys[0];
        long storageAmount = storageFluids.get(fluidId);
        if (maxDrain < storageAmount) {
            if (action == FluidAction.EXECUTE) {
                storageFluids.replace(fluidId, storageAmount - maxDrain);
                onFluidChanged(fluidId, false);
            }
        } else {
            if (action == FluidAction.EXECUTE) {
                storageFluids.remove(fluidId);
                onFluidChanged(fluidId, true);
            }
            maxDrain = (int) storageAmount;
        }
        return new FluidStack(StorageUtils.getFluid(fluidId), maxDrain);
    }



    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (storageEnergies.containsKey("avaritia:forge_energy")) {
            long storageAmount = storageEnergies.get("avaritia:forge_energy");
            long remainingSpaces = Long.MAX_VALUE - storageAmount;
            if (remainingSpaces >= maxReceive) {
                if (!simulate) {
                    storageEnergies.replace("avaritia:forge_energy", storageAmount + maxReceive);
                    onEnergyChanged("avaritia:forge_energy", false);
                }
                return maxReceive;
            } else {
                if (!simulate) {
                    storageEnergies.replace("avaritia:forge_energy", Long.MAX_VALUE);
                    onEnergyChanged("avaritia:forge_energy", false);
                }
                return (int) remainingSpaces;
            }
        } else {
            if (!simulate) {
                storageEnergies.put("avaritia:forge_energy", (long) maxReceive);
                onEnergyChanged("avaritia:forge_energy", true);
            }
            return maxReceive;
        }
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!storageEnergies.containsKey("avaritia:forge_energy")) return 0;
        long storageCount = storageEnergies.get("avaritia:forge_energy");
        if (maxExtract < storageCount) {
            if (!simulate) {
                storageEnergies.replace("avaritia:forge_energy", storageCount - maxExtract);
                onEnergyChanged("avaritia:forge_energy", false);
            }
            return maxExtract;
        } else {
            if (!simulate) {
                storageEnergies.remove("avaritia:forge_energy");
                onEnergyChanged("avaritia:forge_energy", true);
            }
            return (int) storageCount;
        }
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, storageEnergies.getOrDefault("avaritia:forge_energy", 0L));
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return storageEnergies.containsKey("avaritia:forge_energy");
    }

    @Override
    public boolean canReceive() {
        return storageEnergies.getOrDefault("avaritia:forge_energy", 0L) < Long.MAX_VALUE;
    }
}
