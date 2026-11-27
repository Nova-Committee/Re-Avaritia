package committee.nova.mods.avaritia.core.io;
import committee.nova.mods.avaritia.api.common.wrapper.*;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.Optional;
public final class StorageAccess {
    private StorageAccess() {}
    public static Optional<ItemHandler> items(BlockEntity tile, Direction side) { if (tile == null || tile.isRemoved()) return Optional.empty(); if (tile instanceof ItemStorageProvider provider) return Optional.ofNullable(provider.getItemHandler(side)); return tile instanceof Container container ? Optional.of(new ContainerItemHandler(container, side)) : Optional.empty(); }
    public static Optional<ItemHandler> items(ItemStack stack) { return stack.getItem() instanceof ItemStorageProvider provider ? Optional.ofNullable(provider.getItemHandler(stack)) : Optional.empty(); }
    public static Optional<FluidItemHandler> fluids(ItemStack stack) { if (stack.getItem() instanceof FluidStorageProvider provider) return Optional.ofNullable(provider.getFluidHandler(stack)); return stack.getItem() instanceof BucketItem || stack.is(Items.BUCKET) ? Optional.of(new BucketFluidHandler(stack)) : Optional.empty(); }
    public static Optional<EnergyStorage> energy(ItemStack stack) { return stack.getItem() instanceof EnergyStorageProvider provider ? Optional.ofNullable(provider.getEnergyHandler(stack)) : Optional.empty(); }
    public static Optional<FluidHandler> fluids(BlockEntity tile, Direction side) { return tile instanceof FluidStorageProvider provider ? Optional.ofNullable(provider.getFluidHandler(side)) : Optional.empty(); }
    public static Optional<EnergyStorage> energy(BlockEntity tile, Direction side) { return tile instanceof EnergyStorageProvider provider ? Optional.ofNullable(provider.getEnergyHandler(side)) : Optional.empty(); }
}
