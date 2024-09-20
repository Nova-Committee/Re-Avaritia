package committee.nova.mods.avaritia.common.capability.wrappers;

import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @Author: cnlimiter
 * @CreateTime: 2024/8/5 下午11:31
 * @Description:
 */
public class RingWrapper implements ICapabilitySerializable<CompoundTag> {
    ItemStackHandler inv = new ItemStackHandler(81) {
        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !(stack.getItem() instanceof NeutronRingItem) && super.isItemValid(slot, stack);
        }
    };
    private final LazyOptional<ItemStackHandler> inventoryCap = LazyOptional.of(() -> inv);

    public RingWrapper(ItemStack stack, CompoundTag nbt) {
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryCap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return inv.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        inv.deserializeNBT(nbt);
    }
}
