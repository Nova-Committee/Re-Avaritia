package committee.nova.mods.avaritia.api.common.caps.item;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2025/3/6 13:52
 * @Description:
 */
public class ItemCapabilitiesWrapper implements ICapabilitySerializable<CompoundTag> {
    private final ItemCapability<?>[] capabilities;
    private final ItemStack itemStack;

    public ItemCapabilitiesWrapper(ItemStack stack, List<Supplier<ItemCapability<?>>> capabilities) {
        this.itemStack = stack;
        this.capabilities = new ItemCapability[capabilities.size()];

        for (int i = 0; i < capabilities.size(); ++i) {
            ItemCapability<?> cap = (capabilities.get(i)).get();
            this.capabilities[i] = cap;
            cap.setWrapper(this);
        }

    }

    public ItemCapabilitiesWrapper(ItemStack stack, ItemCapability<?>... capabilities) {
        this.itemStack = stack;
        this.capabilities = capabilities;

        for (ItemCapability<?> cap : this.capabilities) {
            cap.setWrapper(this);
        }

    }

    protected ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        for (ItemCapability<?> cap : this.capabilities) {
            if (capability == cap.getCapability()) {
                return cap.getLazyCapability().cast();
            }
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag serializedNBT = new CompoundTag();

        for (ItemCapability<?> cap : this.capabilities) {
            if (cap instanceof IItemCapabilitySerializable serializableCap) {
                serializedNBT.put(serializableCap.getStorageKey(), serializableCap.serializeNBT());
            }
        }

        return serializedNBT;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (ItemCapability<?> cap : this.capabilities) {
            if (cap instanceof IItemCapabilitySerializable serializableCap) {
                if (nbt.contains(serializableCap.getStorageKey())) {
                    serializableCap.deserializeNBT(nbt.get(serializableCap.getStorageKey()));
                }
            }
        }

    }
}
