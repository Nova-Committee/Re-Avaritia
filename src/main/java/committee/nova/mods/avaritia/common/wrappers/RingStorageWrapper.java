//package committee.nova.mods.avaritia.common.wrappers;
//
//import committee.nova.mods.avaritia.api.common.wrapper.OffsetItemStackWrapper;
//import committee.nova.mods.avaritia.common.item.misc.NeutronRingItem;
//import committee.nova.mods.avaritia.util.StorageUtils;
//import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
//import lombok.Getter;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.world.inventory.ContainerData;
//import net.minecraft.world.inventory.DataSlot;
//import net.minecraft.world.item.ItemStack;
//import net.neoforged.neoforge.common.util.INBTSerializable;
//import org.jetbrains.annotations.NotNull;
//
///**
// * @Project: Avaritia
// * @Author: cnlimiter
// * @CreateTime: 2025/3/6 02:14
// * @Description:
// */public class RingStorageWrapper extends OffsetItemStackWrapper implements INBTSerializable<CompoundTag> {
//    private final int rows;
//    public Int2ObjectMap<StorageItem> containers = StorageUtils.newContainers();
//    @Getter private int page = 0;
//
//    public RingStorageWrapper(int rows) {
//        this.rows = rows;
//    }
//
//    @Override
//    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
//        return !(stack.getItem() instanceof NeutronRingItem) && super.isItemValid(slot, stack);
//    }
//
//    @Override
//    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
//        CompoundTag compound = new CompoundTag();
//        StorageUtils.saveAllItems(provider, compound, this.containers);
//        compound.putInt("Page", this.page);
//        return compound;
//    }
//
//    @Override
//    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag compoundTag) {
//        this.containers.clear();
//        StorageUtils.loadAllItems(provider, compoundTag, this.containers);
//        this.page = compoundTag.getInt("Page");
//    }
//
//    @Override
//    protected Int2ObjectMap<StorageItem> getContainers() {
//        return containers;
//    }
//
//    @Override
//    protected int getOffset() {
//        // Ensure the offset is always calculated based on the latest page value
//        return this.rows * 9 * this.page;
//    }
//
//    @Override
//    public int getSlots() {
//        return this.rows * 9;
//    }
//
//    public final DataSlot pageData = new DataSlot() {
//        @Override
//        public int get() {
//            return RingStorageWrapper.this.page;
//        }
//        @Override
//        public void set(int value) {
//            RingStorageWrapper.this.page = value;
//        }
//    };
//
//    public final ContainerData countData = new ContainerData() {
//            @Override public int get(int index) {
//                return index >= 0 && index < RingStorageWrapper.this.getSlots() ? (int) RingStorageWrapper.this.getContainerInSlot(index).getCount() : 0;
//            }
//            @Override public void set(int index, int value) {
//                if (index >= 0 && index < RingStorageWrapper.this.getSlots()) {
//                    RingStorageWrapper.this.getContainerInSlot(index).setCount(Integer.toUnsignedLong(value));
//                }
//            }
//            @Override public int getCount() {
//                return RingStorageWrapper.this.getSlots();
//            }
//    };
//}
