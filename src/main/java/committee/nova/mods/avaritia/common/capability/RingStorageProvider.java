package committee.nova.mods.avaritia.common.capability;

import committee.nova.mods.avaritia.api.common.wrapper.ItemStackWrapper;
import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

/**
 * @Project: Avaritia
 * @author cnlimiter
 * @CreateTime: 2024/8/5 下午11:31
 * @Description:
 */
public class RingStorageProvider implements ICapabilitySerializable<CompoundTag> {
    private final ItemStackWrapper inv;
    private final LazyOptional<ItemStackWrapper> inventoryCap;

    public RingStorageProvider(ItemStack stack, CompoundTag nbt) {
        this.inv = ItemStackWrapper.create(81, builder -> {
            builder.setDefaultSlotLimit(Integer.MAX_VALUE);
            builder.setCanInsert((slot, stack2) -> !(stack2.getItem() instanceof NeutronRingItem));
        });
        this.inventoryCap = LazyOptional.of(() -> inv);
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
